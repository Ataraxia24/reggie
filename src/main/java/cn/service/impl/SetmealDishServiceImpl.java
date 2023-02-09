package cn.service.impl;

import cn.domain.SetmealDish;
import cn.mapper.SetmealDishMapper;
import cn.mapper.SetmealMapper;
import cn.service.SetmealDishService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
