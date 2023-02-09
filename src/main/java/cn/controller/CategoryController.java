package cn.controller;

import cn.common.R;
import cn.domain.Category;
import cn.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService service;

    @PostMapping
    public R<String> save(@RequestBody Category category) {
        service.save(category);
        return R.success("新增分类成功!");
    }

    @GetMapping("/page")
    public R<Page> getByPage(Integer page, Integer pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);

        service.page(pageInfo, wrapper);

        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> updateCategory(@RequestBody Category category) {
        service.updateById(category);
        return R.success("修改成功!");
    }

    @DeleteMapping
    public R<String> deleteCategory(Long id) {
        return service.remove(id);
    }

    @GetMapping("/list")
    public R<List<Category>> choiceCategory(Category category) {

        //通过type显示所有菜品分类名称
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(category.getType() != null, Category::getType, category.getType());
        log.info("type={}",category.getType());

        List<Category> list = service.list(wrapper);

        log.info("{}",list);
        return R.success(list);
    }
}
