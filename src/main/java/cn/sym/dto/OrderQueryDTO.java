package cn.sym.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 查询订单详情请求参数对象
 * 
 * @author user
 */
@Data
public class OrderQueryDTO {

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /**
     * 页码，默认为1
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    /**
     * 每页条数，默认为10
     */
    @Min(value = 1, message = "每页条数最小为1")
    private Integer size = 10;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 订单状态
     */
    private Integer status;
}