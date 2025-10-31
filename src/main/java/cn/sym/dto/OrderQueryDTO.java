package cn.sym.dto;

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
}