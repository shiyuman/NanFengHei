package cn.sym.service.impl;

import cn.sym.dto.ExternalCallRequestDTO;
import cn.sym.entity.OrderInfo;
import cn.sym.repository.OrderInfoRepository;
import cn.sym.common.response.RestResult;
import cn.sym.service.ExternalCallService;
import cn.sym.utils.HttpClientUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * <p>
 *   外部接口调用服务实现类
 * </p>
 * @author user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalCallServiceImpl implements ExternalCallService {

    private final OrderInfoRepository orderInfoRepository;

    @Value("${external.payment.url}")
    private String paymentUrl;

    @Value("${external.logistics.url}")
    private String logisticsUrl;

    private final ObjectMapper objectMapper;

    @Override
    public RestResult<Object> callPaymentGateway(ExternalCallRequestDTO request) {
        try {
            // 查询订单是否存在
            OrderInfo orderInfo = orderInfoRepository.selectById(request.getOrderId());
            if (orderInfo == null) {
                return new RestResult<>("000001", "订单信息不存在");
            }

            // 组装支付请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("orderId", request.getOrderId());
            params.put("amount", request.getAmount());
            params.put("notifyUrl", request.getNotifyUrl());

            // 发起HTTP POST请求到第三方支付网关
            String responseJson = HttpClientUtil.post(paymentUrl, params);

            // 解析响应并返回结果
            RestResult<Object> result = objectMapper.readValue(responseJson, 
                objectMapper.getTypeFactory().constructParametricType(RestResult.class, Object.class));
            return result;
        } catch (Exception e) {
            log.error("调用第三方支付接口异常:", e);
            return new RestResult<>("999999", "系统异常");
        }
    }

    @Override
    public RestResult<Object> queryLogisticsInfo(ExternalCallRequestDTO request) {
        try {
            // 校验参数是否为空
            if (request.getWaybillNo() == null || request.getWaybillNo().isEmpty()) {
                return new RestResult<>("000001", "参数不能为空");
            }
            if (request.getLogisticsCode() == null || request.getLogisticsCode().isEmpty()) {
                return new RestResult<>("000001", "参数不能为空");
            }

            // 构造请求URL
            String fullUrl = logisticsUrl + "?waybillNo=" + request.getWaybillNo() +
                    "&logisticsCode=" + request.getLogisticsCode();

            // 发送GET请求至物流接口
            String responseJson = HttpClientUtil.get(fullUrl);

            // 解析响应并返回结果
            RestResult<Object> result = objectMapper.readValue(responseJson,
                objectMapper.getTypeFactory().constructParametricType(RestResult.class, Object.class));
            return result;
        } catch (Exception e) {
            log.error("调用物流接口异常:", e);
            return new RestResult<>("999999", "系统异常");
        }
    }
}