package cn.sym.service;

import cn.sym.dto.ExternalCallRequestDTO;
import cn.sym.response.RestResult;

/**
 * <p>
 *   外部接口调用服务接口
 * </p>
 * @author user
 */
public interface ExternalCallService {

    /**
     * 调用第三方支付接口
     *
     * @param request 请求参数
     * @return RestResult 结果包装对象
     */
    RestResult<Object> callPaymentGateway(ExternalCallRequestDTO request);

    /**
     * 调用物流接口查询运单信息
     *
     * @param request 请求参数
     * @return RestResult 结果包装对象
     */
    RestResult<Object> queryLogisticsInfo(ExternalCallRequestDTO request);
}