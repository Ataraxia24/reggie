package cn.controller;

import cn.common.R;
import cn.domain.Category;
import cn.domain.Dish;
import cn.domain.DishFlavor;
import cn.domain.Employee;
import cn.dto.DishDto;
import cn.service.CategoryService;
import cn.service.DishFlavorService;
import cn.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品页面采用redis缓存技术
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService service;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService flavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {           //菜品单独添加, 需要获取菜品总信息, 这里采用删除重置
        service.saveWithFlavor(dishDto);

        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功!");
    }

    @GetMapping("/page")
    public R<Page> getByPage(Integer page, Integer pageSize, String name) {         //字段与请求需对应
        //本页面展示包含两表, 从各表查询注入到dto中

        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);             //复制到的空表

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper();
        wrapper.like(StringUtils.hasText(name), Dish::getName, name);       //name是否为空, 空为false, 不执行该语句

        service.page(pageInfo, wrapper);

        //拷贝page信息, 忽略records,需要单独对records做处理: 页面展示的list集合
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        //获取原records进行修改
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item)->{                  //流循环遍历
            DishDto dishDto = new DishDto();

            //拷贝dish中的全部信息到dto中
            BeanUtils.copyProperties(item, dishDto);

            //获取本条信息id
            Long categoryId = item.getCategoryId();

            //通过id获取category中的分类名
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();

            //更改dto中的categoryName
            dishDto.setCategoryName(categoryName);

            return dishDto;                 //返回所需对象而不是item
        }).collect(Collectors.toList());

        //完成了对dto表中所有数据的赋值, 向页面展示的records依然为空, 注入
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {       //修改时数据回显
        return service.getByIdWithFlavor(id);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        service.updateWithFlavor(dishDto);

        //当后台数据更改后, 迭代缓存内容
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("修改菜品成功!");
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam("ids") Long[] id) {           //批量处理
        log.info("status={}, id={}", status, id);

        Set<Long> set = new HashSet<>();                //用来存储不同的菜品相同的分类id, set无法重复

        for (Long ids : id) {
            LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
            Dish dish = new Dish();
            dish.setStatus(status);

            wrapper.eq(Dish::getId, ids);

            Dish byId = service.getById(ids);
            set.add(byId.getCategoryId());

            service.update(dish, wrapper);
        }

        //对更改状态的菜品进行更新缓存, 根据分类的key缓存该分类下的所有菜品, 无法缓存单个菜品
        for (Long sets : set) {
            String key = "dish_" + sets + "_1";                 //固定1, 下方缓存中只会存储启售菜品
            redisTemplate.delete(key);
        }

        return R.success("修改状态成功");
    }

    @DeleteMapping
    public R<String> delete(Long[] ids) {
        service.deleteWithFlavor(ids);

        return R.success("删除成功!");
    }

    @GetMapping("/list")
    public R<List<DishDto>> selectDish(Dish dish) {         //后台无显示flavor, 前台需要选择口味规格
        //菜品页面显示口味数据
        List<DishDto> dishDtoList = null;

        //根据页面左侧分类id获取所有对应菜品
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();

        //仅显示未售罄菜品
        wrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId, dish.getCategoryId());
        wrapper.eq(Dish::getStatus, 1);
        wrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = service.list(wrapper);

        //使用缓存技术, 查看当前是否能请求到数据
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            //根据id获取口味信息
            Long id = item.getId();

            LambdaQueryWrapper<DishFlavor> flavorWrapper = new LambdaQueryWrapper<>();
            flavorWrapper.eq(id != 0, DishFlavor::getDishId, id);

            //将查询的口味信息注入到dto中
            List<DishFlavor> flavorList = flavorService.list(flavorWrapper);
            dishDto.setFlavors(flavorList);

            return dishDto;
        }).collect(Collectors.toList());

        //查询noSql数据库, 缓存内容实现为60m
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
