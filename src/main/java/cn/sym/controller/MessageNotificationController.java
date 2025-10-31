package cn.sym.controller;

import cn.sym.dto.OperationLogDTO;
import cn.sym.dto.OrderStatusChangeDTO;
import cn.sym.response.RestResult;
import cn.sym.service.MessageNotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知控制器
 *
 * @author user
 */
@Slf4j
@Api(tags = "消息通知管理")
@RestController
@RequestMapping("/message")
public class MessageNotificationController {

    @Autowired
    private MessageNotificationService messageNotificationService;

    /**
     * 发送订单状态变更通知
     *
     * @param dto 订单状态变更参数
     * @return 响应结果
     */
    @PostMapping("/send-order-status-change-notice")
    @ApiOperation("发送订单状态变更通知")
    public RestResult<Boolean> sendOrderStatusChangeNotice(@RequestBody @Valid OrderStatusChangeDTO dto) {
        log.info("接收到发送订单状态变更通知请求，参数: {}", dto);
        return messageNotificationService.sendOrderStatusChangeNotice(dto);
    }

    /**
     * 记录操作日志
     *
     * @param dto 操作日志参数
     * @return 响应结果
     */
    @PostMapping("/record-operation-log")
    @ApiOperation("记录操作日志")
    public RestResult<Boolean> recordOperationLog(@RequestBody @Valid OperationLogDTO dto) {
        log.info("接收到记录操作日志请求，参数: {}", dto);
        return messageNotificationService.recordOperationLog(dto);
    }
}