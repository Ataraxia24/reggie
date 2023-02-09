package cn.service.impl;

import cn.common.R;
import cn.domain.Dish;
import cn.domain.DishFlavor;
import cn.dto.DishDto;
import cn.mapper.DishMapper;
import cn.service.DishFlavorService;
import cn.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void saveWithFlavor(DishDto dishDto) {          //表现层无法直接注入两表需要的数据, 在service层手动注入

        //将获取的数据分开存入dish\dishFlavor表
        this.save(dishDto);

        Long id = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        //list.stream()与foreach一样遍历, map()用于映射每个元素对应的结果, item对应结果对象
        flavors = flavors.stream().map((item)->{
            item.setDishId(id);                     //循环修改dishId
            return item;
        }).collect(Collectors.toList());            //转为list集合

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public R<DishDto> getByIdWithFlavor(Long id) {            //数据回显
        DishDto dishDto = new DishDto();

        //通过id获得dish表和flavor表数据
        Dish dish = this.getById(id);

        //拷贝到dto中
        BeanUtils.copyProperties(dish, dishDto);

        //将dto的flavors注入对应信息
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, dish.getId());        //flavor表中的dishId与dish表中的id

        List<DishFlavor> flavorList = dishFlavorService.list(wrapper);
        dishDto.setFlavors(flavorList);

        return R.success(dishDto);
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表
        this.updateById(dishDto);

        //更新flavor表 : 若将当前口味全部更换, 表中原口味数据还会存在, 每次更换口味前需删除
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(wrapper);

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)-> {
            item.setDishId(dishDto.getId());          //设置id
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

    }

    @Override
    public void deleteWithFlavor(Long[] ids) {
        Set<Long> set = new HashSet<>();                //用来存储不同的菜品相同的分类id, set无法重复

        for (Long id : ids) {
            //删除之前清除缓存, 删除之后为null
            Dish byId = this.getById(id);
            set.add(byId.getCategoryId());

            this.removeById(id);

            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, id);


            dishFlavorService.remove(wrapper);
        }

        //对更改状态的菜品进行更新缓存, 根据分类的key缓存该分类下的所有菜品, 无法缓存单个菜品
        for (Long sets : set) {
            String key = "dish_" + sets + "_1";                 //固定1, 下方缓存中只会存储启售菜品
            redisTemplate.delete(key);
        }
    }
}
