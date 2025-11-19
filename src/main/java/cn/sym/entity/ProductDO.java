package cn.sym.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import cn.sym.common.annotation.SensitiveData;
import cn.sym.common.annotation.SensitiveType;


/**
 * 商品信息实体类
 * @author user
 */
@NoArgsConstructor
@AllArgsConstructor
@TableName("product_info")
@Data
public class ProductDO {

    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 单价
     */
    @SensitiveData(SensitiveType.PRICE)
    private Integer price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 上下架状态：1-上架，0-下架
     */
    private Integer status;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    
    /**
     * 版本号，用于乐观锁
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
    
    /**
     * 逻辑删除标识，0-未删除，1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
