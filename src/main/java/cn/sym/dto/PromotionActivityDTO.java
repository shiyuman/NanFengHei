package cn.sym.dto;

import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *   促销活动传输对象
 * </p>
 * @author user
 */
@Data
public class PromotionActivityDTO {

    /**
     * 活动ID（修改时必填）
     */
    private Long id;

    /**
     * 活动名称（新增时必填）
     */
    @NotNull(message = "活动名称不能为空")
    private String name;

    /**
     * 活动类型（新增时必填）：1-限时折扣，2-秒杀
     */
    @NotNull(message = "活动类型不能为空")
    private Integer activityType;

    /**
     * 活动开始时间（新增时必填）
     */
    @NotNull(message = "活动开始时间不能为空")
    private Date startTime;

    /**
     * 活动结束时间（新增时必填）
     */
    @NotNull(message = "活动结束时间不能为空")
    private Date endTime;

    /**
     * 活动状态：1-进行中，2-已结束，3-已暂停
     */
    private Integer status;
}