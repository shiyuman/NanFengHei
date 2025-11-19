package cn.sym.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import lombok.Data;

/**
 * 限购配置实体类
 *
 * @author user
 */
@Data
@TableName("limit_purchase")
public class LimitPurchaseDO {

    /**
     * 限购记录唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 最大购买数量
     */
    private Integer maxQuantity;

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
}