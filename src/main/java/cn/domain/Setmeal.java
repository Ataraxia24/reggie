package cn.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐
 */
@Data
/*@Api                     用在请求的类上，例如Controller，表示对类的说明
@ApiOperation               用在请求的方法上，说明方法的用途、作用
ApilmplicitParams           用在请求的方法上，表示一组参数说明
@ApilmplicitParam           用在@ApilmplicitParams注解中，指定一个请求参数的各个方面
 */

@ApiModel("套餐")         //用在类上，通常是实体类，表示一个返回响应数据的信息
public class Setmeal implements Serializable {

    private static final long serialVersionUID = 1L;

    //用在属性上，描述响应类的属性
    @ApiModelProperty("主键") 
    private Long id;


    //分类id
    @ApiModelProperty("分类id")
    private Long categoryId;


    //套餐名称
    @ApiModelProperty("套餐名称")
    private String name;


    //套餐价格
    @ApiModelProperty("套餐价格")
    private BigDecimal price;


    //状态 0:停用 1:启用
    @ApiModelProperty("状态")
    private Integer status;


    //编码
    @ApiModelProperty("套餐编号")
    private String code;


    //描述信息
    @ApiModelProperty("描述信息")
    private String description;


    //图片
    @ApiModelProperty("图片")
    private String image;


    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    @TableField(fill = FieldFill.INSERT)
    private Long createUser;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;


    //是否删除
    private Integer isDeleted;
}
