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
@Data
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
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改时间
     */
    private Date updateTime;
}