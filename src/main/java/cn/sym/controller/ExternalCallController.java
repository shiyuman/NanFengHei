package cn.sym.controller;

import cn.sym.dto.ExternalCallRequestDTO;
import cn.sym.common.response.RestResult;
import cn.sym.service.ExternalCallService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *   外部接口调用控制器
 * </p>
 * @author user
 */
@Slf4j
@Api(tags = "外部接口调用管理")
@RestController
@RequestMapping("/external-call")
public class ExternalCallController {

    @Autowired
    private ExternalCallService externalCallService;

    /**
     * 调用第三方支付接口
     *
     * @param request 请求参数
     * @return RestResult 结果包装对象
     */
    @PostMapping("/payment")
    @ApiOperation("调用第三方支付接口")
    public RestResult<Object> callPaymentGateway(@RequestBody @Validated ExternalCallRequestDTO request) {
        log.info("调用第三方支付接口，入参：{}", request);
        return externalCallService.callPaymentGateway(request);
    }

    /**
     * 调用物流接口查询运单信息
     *
     * @param request 请求参数
     * @return RestResult 结果包装对象
     */
    @GetMapping("/logistics")
    @ApiOperation("调用物流接口查询运单信息")
    public RestResult<Object> queryLogisticsInfo(@Validated ExternalCallRequestDTO request) {
        log.info("调用物流接口查询运单信息，入参：{}", request);
        return externalCallService.queryLogisticsInfo(request);
    }
}