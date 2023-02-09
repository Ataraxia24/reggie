package cn.service;

import cn.common.R;
import cn.domain.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CategoryService extends IService<Category> {
    R<String> remove(Long id);
}
