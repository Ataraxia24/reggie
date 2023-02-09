package cn.service.impl;

import cn.domain.DishFlavor;
import cn.mapper.DishFlavorMapper;
import cn.service.DishFlavorService;
import cn.service.DishService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
