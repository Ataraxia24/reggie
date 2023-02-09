package cn.service.impl;

import cn.common.R;
import cn.domain.Category;
import cn.domain.Dish;
import cn.domain.Setmeal;
import cn.mapper.CategoryMapper;
import cn.service.CategoryService;
import cn.service.DishService;
import cn.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Override
    public R<String> remove(Long id) {       //删除菜品分类, 若分类包含菜品和套餐则无法删除

        //查看当前id在dish中是否存在
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishWrapper);
        if (dishCount > 0) {
            return R.fail("当前分类下关联了菜品，不能删除");
        }

        //查看当前id在meal中是否存在
        LambdaQueryWrapper<Setmeal> mealWrapper = new LambdaQueryWrapper<>();
        mealWrapper.eq(Setmeal::getCategoryId, id);
        int mealCount = setmealService.count(mealWrapper);
        if (mealCount > 0) {
            return R.fail("当前分类下关联了套餐，不能删除");
        }

        super.removeById(id);
        return R.success("删除成功!");
    }
}
