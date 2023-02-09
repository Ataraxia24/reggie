package cn.controller;

import cn.common.R;
import cn.domain.*;
import cn.dto.DishDto;
import cn.dto.SetmealDto;
import cn.service.CategoryService;
import cn.service.DishFlavorService;
import cn.service.SetmealDishService;
import cn.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
/**
 * 套餐页面采用spring cache缓存技术
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService service;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService mealDishService;

    /**
     * CachePut：将方法返回值放入缓存
     * value：缓存的名称，每个缓存名称下面可以有多个key
     * key：缓存的key
     */
    @PostMapping
    //@CachePut(value = "userCache",key = "#setmealDto.categoryId")             缓存会有重复数据
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {

        service.saveWithDish(setmealDto);
        return R.success("新增套餐成功!");
    }

    @GetMapping("/page")
    public R<Page> getByPage(Integer page, Integer pageSize, String name) {
        //查找数据, 需要展示额外套餐分类, 注入到总表dto

        //添加查询条件
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Setmeal::getName, name);

        Page<Setmeal> pageMeal = new Page<>(page, pageSize);        //无法直接通过dto设置page, dto无法连接后台数据库
        service.page(pageMeal, wrapper);

        Page<SetmealDto> pageDto = new Page<>();

        //单独设置records, 循环设置套餐分类名称
        BeanUtils.copyProperties(pageMeal, pageDto, "records");

        List<Setmeal> records = pageMeal.getRecords();          //获取meal表的所有数据
        List<SetmealDto> list = records.stream().map((item)->{          //现在是meal表
            SetmealDto dto = new SetmealDto();

            //拷贝所有数据
            BeanUtils.copyProperties(item, dto);

            //获取分类名
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            dto.setCategoryName(category.getName());

            return dto;                     //返回所需对象
        }).collect(Collectors.toList());

        //将list集合添加到其records中
        pageDto.setRecords(list);

        return R.success(pageDto);
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {                //回显数据
        return service.getWithDish(id);
    }

    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam("ids") Long[] id) {           //批量处理
        log.info("status={}, id={}", status, id);

        for (Long ids : id) {
            LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
            Setmeal setmeal = new Setmeal();
            setmeal.setStatus(status);

            wrapper.eq(Setmeal::getId, ids);
            service.update(setmeal, wrapper);
        }

        return R.success("修改状态成功");
    }

    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        service.updateWithDish(setmealDto);
        return R.success("修改成功");
    }

    /**
     * CacheEvict：清理指定缓存
     * value：缓存的名称，每个缓存名称下面可以有多个key
     * key：缓存的key
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(Long[] ids) {
        service.deleteWithDish(ids);

        return R.success("删除成功!");
    }

    /**
     * Cacheable：在方法执行前spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据；若没有数据，调用方法并将方法返回值放到缓存中
     * value：缓存的名称，每个缓存名称下面可以有多个key
     * key：缓存的key
     * condition：条件，满足条件时才缓存数据
     * unless：满足条件则不缓存
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = service.list(queryWrapper);

        return R.success(list);
    }

}
