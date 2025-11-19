package cn.sym.service.impl;

import cn.sym.common.constant.ResultCodeConstant;
import cn.sym.common.response.RestResult;
import cn.sym.dto.CustomerServiceMessageDTO;
import cn.sym.dto.OrderStatusChangeDTO;
import cn.sym.dto.WebSocketConnectDTO;
import cn.sym.entity.ChatMessage;
import cn.sym.entity.OrderInfo;
import cn.sym.entity.UserDO;
import cn.sym.repository.ChatMessageMapper;
import cn.sym.repository.OrderInfoMapper;
import cn.sym.repository.UserMapper;
import cn.sym.service.WebSocketService;
import cn.sym.utils.SessionUtil;
import java.util.Date;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final UserMapper userMapper;

    private final OrderInfoMapper orderInfoMapper;

    private final ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional
    public RestResult<Void> connect(WebSocketConnectDTO connectDTO) {
        UserDO userDO = userMapper.selectById(connectDTO.getUserId());
        if (userDO == null || !SessionUtil.isLoggedIn(connectDTO.getUserId())) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "用户未登录");
        }

        SessionUtil.addSession(connectDTO.getUserId(), connectDTO.getSessionId());
        return new RestResult<>(ResultCodeConstant.CODE_000000, "连接成功");
    }

    @Override
    @Transactional
    public RestResult<Void> pushOrderStatusChange(OrderStatusChangeDTO orderStatusChangeDTO) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderStatusChangeDTO.getOrderId());
        if (orderInfo == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "订单不存在");
        }

        UserDO userDO = userMapper.selectById(orderInfo.getUserId());
        if (userDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "用户不存在");
        }

        if (!SessionUtil.isUserOnline(orderInfo.getUserId())) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "用户未在线");
        }

        SessionUtil.sendMessage(orderInfo.getUserId(), orderStatusChangeDTO.getMessage());
        return new RestResult<>(ResultCodeConstant.CODE_000000, "消息推送成功");
    }

    @Override
    @Transactional
    public RestResult<Void> sendCustomerServiceMessage(CustomerServiceMessageDTO messageDTO) {
        UserDO fromUserDO = userMapper.selectById(messageDTO.getFromUserId());
        UserDO toUserDO = userMapper.selectById(messageDTO.getToUserId());
        if (fromUserDO == null || toUserDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "用户信息无效");
        }

        ChatMessage chatMessage = new ChatMessage();
        BeanUtils.copyProperties(messageDTO,chatMessage);
        chatMessage.setCreateTime(new Date());
        chatMessageMapper.insert(chatMessage);

        if (SessionUtil.isUserOnline(messageDTO.getToUserId())) {
            SessionUtil.sendMessage(messageDTO.getToUserId(), messageDTO.getContent());
        }

        return new RestResult<>(ResultCodeConstant.CODE_000000, "消息发送成功");
    }

    @Override
    @Transactional
    public RestResult<Void> disconnect(WebSocketConnectDTO disconnectDTO) {
        UserDO userDO = userMapper.selectById(disconnectDTO.getUserId());
        if (userDO == null || !SessionUtil.isUserOnline(disconnectDTO.getUserId())) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "用户未连接");
        }

        SessionUtil.removeSession(disconnectDTO.getUserId(), disconnectDTO.getSessionId());
        return new RestResult<>(ResultCodeConstant.CODE_000000, "连接已断开");
    }
}