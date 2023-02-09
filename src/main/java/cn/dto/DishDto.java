package cn.dto;

import cn.domain.Dish;
import cn.domain.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {
    //前台新增请求的数据包含口味, 创建一个包含前端共同数据的类
    private List<DishFlavor> flavors = new ArrayList<>();              //口味数据

    private String categoryName;            //页面需要单独展示的数据, dish表中无, 通过联合表单独安排数据
}
