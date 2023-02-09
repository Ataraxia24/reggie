package cn.dto;

import cn.domain.Setmeal;
import cn.domain.SetmealDish;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {       //两表合并类

    private List<SetmealDish> setmealDishes = new ArrayList<>();

    private String categoryName;            //额外展示类
}
