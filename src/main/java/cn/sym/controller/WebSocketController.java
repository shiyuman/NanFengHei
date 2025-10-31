package cn.sym.controller;

import cn.sym.common.response.RestResult;
import cn.sym.dto.CustomerServiceMessageDTO;
import cn.sym.dto.OrderStatusChangeDTO;
import cn.sym.dto.WebSocketConnectDTO;
import cn.sym.service.WebSocketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api("WebSocket管理")
@RequestMapping("websocket")
@RestController
public class WebSocketController {

    @Autowired
    private WebSocketService webSocketService;

    @PostMapping("/connect")
    @ApiOperation("用户连接WebSocket")
    public RestResult<Void> connect(@RequestBody @Validated WebSocketConnectDTO connectDTO) {
        return webSocketService.connect(connectDTO);
    }

    @PostMapping("/order-status-change")
    @ApiOperation("推送订单状态变更消息")
    public RestResult<Void> pushOrderStatusChange(@RequestBody @Validated OrderStatusChangeDTO orderStatusChangeDTO) {
        return webSocketService.pushOrderStatusChange(orderStatusChangeDTO);
    }

    @PostMapping("/send-customer-service-message")
    @ApiOperation("客服聊天消息发送")
    public RestResult<Void> sendCustomerServiceMessage(@RequestBody @Validated CustomerServiceMessageDTO messageDTO) {
        return webSocketService.sendCustomerServiceMessage(messageDTO);
    }

    @PostMapping("/disconnect")
    @ApiOperation("断开WebSocket连接")
    public RestResult<Void> disconnect(@RequestBody @Validated WebSocketConnectDTO disconnectDTO) {
        return webSocketService.disconnect(disconnectDTO);
    }
}