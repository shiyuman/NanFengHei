package cn.sym.entity;

import java.util.Date;
import javax.persistence.*;
import org.apache.ibatis.type.Alias;
import lombok.*;
import java.math.BigDecimal;

/**
 * 订单信息实体类
 *
 * @author user
 */
@NoArgsConstructor
@AllArgsConstructor
@Alias("OrderInfo")
@Entity
@Table(name = "order_info")
@Data
public class OrderInfo {

    /**
     * 订单唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 下单用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 订单编号
     */
    @Column(name = "order_no")
    private String orderNo;

    /**
     * 订单总金额
     */
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    /**
     * 配送方式：1-自取，2-快递
     */
    @Column(name = "delivery_type")
    private Integer deliveryType;

    /**
     * 订单状态：1-待支付，2-已支付，3-已完成，4-已取消
     */
    private Integer status;

    /**
     * 创建人
     */
    @Column(name = "create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 修改人
     */
    @Column(name = "update_by")
    private String updateBy;

    /**
     * 修改时间
     */
    @Column(name = "update_time")
    private Date updateTime;
}
