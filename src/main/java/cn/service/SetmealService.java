package cn.service;

import cn.common.R;
import cn.domain.Setmeal;
import cn.dto.SetmealDto;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);
    R<SetmealDto> getWithDish(Long id);
    void updateWithDish(SetmealDto setmealDto);
    void deleteWithDish(Long[] ids);
}
