package cn.service.impl;

import cn.common.R;
import cn.domain.DishFlavor;
import cn.domain.Setmeal;
import cn.domain.SetmealDish;
import cn.dto.SetmealDto;
import cn.mapper.SetmealMapper;
import cn.service.SetmealDishService;
import cn.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);                          //新增套餐

        List<SetmealDish> dishList = setmealDto.getSetmealDishes();      //单独获取菜品集合

        //循环添加setmealDish表中所绑定的套餐id
        dishList = dishList.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(dishList);
    }

    @Override
    public R<SetmealDto> getWithDish(Long id) {
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();

        SetmealDto dto = new SetmealDto();
        Setmeal setmeal = this.getById(id);

        //拷贝setmeal的属性
        BeanUtils.copyProperties(setmeal, dto);

        //拷贝setmeaDish的集合注入到dto的集合中
        wrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> dishes = setmealDishService.list(wrapper);

        dto.setSetmealDishes(dishes);

        return R.success(dto);
    }

    @Override
    public void updateWithDish(SetmealDto dto) {
        this.updateById(dto);

        //获取当前要删除的id
        Long id = dto.getId();

        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, id);

        //重置
        setmealDishService.remove(wrapper);

        List<SetmealDish> dishes = dto.getSetmealDishes();
        dishes = dishes.stream().map((item)->{
            item.setSetmealId(id);
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(dishes);
    }

    @Override
    public void deleteWithDish(Long[] ids) {

        for (Long id : ids) {
            this.removeById(id);

            LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SetmealDish::getSetmealId, id);

            setmealDishService.remove(wrapper);
        }
    }
}
