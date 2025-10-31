package cn.sym.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 *   促销活动实体类
 * </p>
 * @author user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("promotion_activity")
public class PromotionActivityDO {

    /**
     * 促销活动唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 活动类型：1-限时折扣，2-秒杀
     */
    private Integer activityType;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

    /**
     * 活动状态：1-进行中，2-已结束，3-已暂停
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