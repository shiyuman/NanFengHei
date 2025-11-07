package cn.sym.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import cn.sym.common.annotation.SensitiveData;
import cn.sym.common.annotation.SensitiveType;

/**
 * 早鸟票预售配置实体类
 *
 * @author user
 */
@Data
@TableName("pre_sale_ticket")
public class PreSaleTicketDO {

    /**
     * 早鸟票唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 预售开始时间
     */
    private Date saleStartTime;

    /**
     * 预售结束时间
     */
    private Date saleEndTime;

    /**
     * 预售价格
     */
    @SensitiveData(SensitiveType.PRICE)
    private BigDecimal prePrice;

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