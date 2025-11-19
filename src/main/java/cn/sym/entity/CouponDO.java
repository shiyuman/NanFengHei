package cn.sym.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import cn.sym.common.annotation.SensitiveData;
import cn.sym.common.annotation.SensitiveType;

/**
 * <p>
 *   优惠券实体类
 * </p>
 * @author user
 */
@Data  // Lombok注解，自动生成getter/setter方法
@TableName("coupon_info")
public class CouponDO {

    /**
     * 优惠券唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 类型：1-满减券，2-折扣券
     */
    private Integer type;

    /**
     * 减免金额或折扣比例
     */
    @SensitiveData(SensitiveType.AMOUNT)
    private BigDecimal discountAmount;

    /**
     * 最低消费金额
     */
    @SensitiveData(SensitiveType.AMOUNT)
    private BigDecimal minAmount;

    /**
     * 生效开始时间
     */
    private Date startTime;

    /**
     * 生效结束时间
     */
    private Date endTime;

    /**
     * 可用状态：1-未使用，2-已使用，3-已过期
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
     * 逻辑删除标识，0-未删除，1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}