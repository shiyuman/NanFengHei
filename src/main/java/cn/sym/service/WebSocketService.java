package cn.sym.service;

import cn.sym.common.response.RestResult;
import cn.sym.dto.CustomerServiceMessageDTO;
import cn.sym.dto.OrderStatusChangeDTO;
import cn.sym.dto.WebSocketConnectDTO;

public interface WebSocketService {
    RestResult<Void> connect(WebSocketConnectDTO connectDTO);
    RestResult<Void> pushOrderStatusChange(OrderStatusChangeDTO orderStatusChangeDTO);
    RestResult<Void> sendCustomerServiceMessage(CustomerServiceMessageDTO messageDTO);
    RestResult<Void> disconnect(WebSocketConnectDTO disconnectDTO);
}