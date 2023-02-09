package cn.service;

import cn.common.R;
import cn.domain.Dish;
import cn.dto.DishDto;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

@Transactional          //多表操作添加事务
public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);
    R<DishDto> getByIdWithFlavor(Long id);
    void updateWithFlavor(DishDto dishDto);
    void deleteWithFlavor(Long[] id);
}
