package cn.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private Integer type;
    private String name;
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)                            //注解填充属性       //当一个属性值在插入和更新时反复修改为固定值时, 可以使用属性填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
