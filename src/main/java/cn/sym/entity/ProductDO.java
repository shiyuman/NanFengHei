package cn.sym.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;


/**
 * 商品信息实体类
 * @author user
 */
@NoArgsConstructor
@AllArgsConstructor
@TableName("product_info")
@Data
@ApiModel(description = "商品信息实体类")
public class ProductDO {

    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "商品ID")
    private Long id;

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称")
    private String name;

    /**
     * 商品描述
     */
    @ApiModelProperty(value = "商品描述")
    private String description;

    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID")
    private Long categoryId;

    /**
     * 单价
     */
    @ApiModelProperty(value = "单价")
    private Integer price;

    /**
     * 库存数量
     */
    @ApiModelProperty(value = "库存数量")
    private Integer stock;

    /**
     * 上下架状态：1-上架，0-下架
     */
    @ApiModelProperty(value = "上下架状态：1-上架，0-下架")
    private Integer status;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "修改人")
    private String updateBy;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
    
    /**
     * 版本号，用于乐观锁
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "版本号")
    private Integer version;
    
    /**
     * 逻辑删除标识，0-未删除，1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "逻辑删除标识，0-未删除，1-已删除")
    private Integer deleted;
}
