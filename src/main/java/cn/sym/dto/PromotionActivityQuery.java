package cn.sym.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *   促销活动查询参数对象
 * </p>
 * @author user
 */
@Data
public class PromotionActivityQuery {

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    private Long id;

    /**
     * 页码，默认为1
     */
    private Integer pageNo = 1;

    /**
     * 每页大小，默认为10
     */
    private Integer pageSize = 10;
}