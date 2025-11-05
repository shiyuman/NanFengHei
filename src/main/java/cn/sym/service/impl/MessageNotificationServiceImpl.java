package cn.sym.service.impl;

import cn.sym.dto.OperationLogDTO;
import cn.sym.dto.OrderStatusChangeDTO;
import cn.sym.entity.OrderDO;
import cn.sym.entity.UserDO;
import cn.sym.common.exception.BusinessException;
import cn.sym.repository.OrderMapper;
import cn.sym.repository.UserMapper;
import cn.sym.common.response.RestResult;
import cn.sym.common.response.ResultCodeConstant;
import cn.sym.service.MessageNotificationService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.messaging.Message;

/**
 * 消息通知服务实现类
 *
 * @author user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageNotificationServiceImpl implements MessageNotificationService {

    private final OrderMapper orderMapper;

    private final UserMapper userMapper;

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public RestResult<Boolean> sendOrderStatusChangeNotice(OrderStatusChangeDTO dto) {
        log.info("开始发送订单状态变更通知，订单ID: {}, 订单编号: {}", dto.getOrderId(), dto.getOrderNo());

        // 校验订单是否存在且状态是否合法
        OrderDO orderDO = orderMapper.selectById(dto.getOrderId());
        if (orderDO == null || !Objects.equals(orderDO.getOrderNo(), dto.getOrderNo())) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 判断订单状态是否合法（这里假设只有1、2、3、4四个状态）
        if (!isValidOrderStatus(dto.getStatus())) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 异步发送消息到RocketMQ队列
        try {
            // 构建带标签的消息，根据订单状态设置不同标签
            String destination = "order-status-change-topic:" + getStatusTag(dto.getStatus());
            Message<OrderStatusChangeDTO> message = MessageBuilder.withPayload(dto)
                    .setHeader("status", dto.getStatus())
                    .setHeader("orderId", dto.getOrderId())
                    .build();
            
            // 同步发送消息
            SendResult sendResult = rocketMQTemplate.syncSend(destination, message);
            
            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                log.info("订单状态变更消息已成功投递至RocketMQ队列，消息ID: {}", sendResult.getMsgId());
            } else {
                log.warn("订单状态变更消息投递至RocketMQ队列失败，状态: {}", sendResult.getSendStatus());
                throw new BusinessException(ResultCodeConstant.CODE_000002, "发送通知失败");
            }
        } catch (Exception e) {
            log.error("发送订单状态变更通知失败", e);
            throw new BusinessException(ResultCodeConstant.CODE_000002, "发送通知失败: " + e.getMessage());
        }

        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }

    @Override
    public RestResult<Boolean> recordOperationLog(OperationLogDTO dto) {
        log.info("开始记录操作日志，用户ID: {}", dto.getUserId());

        // 校验用户是否存在
        UserDO userDO = userMapper.selectById(dto.getUserId());
        if (userDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 构建日志信息并异步投递到RocketMQ
        try {
            // 构建带标签的消息，根据操作类型设置不同标签
            String destination = "operation-log-topic:" + getOperationTag(dto.getOperationType());
            Message<OperationLogDTO> message = MessageBuilder.withPayload(dto)
                    .setHeader("operationType", dto.getOperationType())
                    .setHeader("userId", dto.getUserId())
                    .build();
            
            // 异步发送消息，带回调处理
            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("操作日志已成功投递至RocketMQ队列，消息ID: {}", sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("操作日志投递至RocketMQ队列失败", throwable);
                }
            }, 5000); // 5秒超时
            
            log.info("操作日志发送请求已提交");
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
            throw new BusinessException(ResultCodeConstant.CODE_000002, "记录日志失败: " + e.getMessage());
        }

        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }
    
    /**
     * 发送延迟消息示例 - 用于订单超时处理
     * @param dto 订单状态变更DTO
     * @param delayLevel 延迟级别 (1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h)
     * @return
     */
    public RestResult<Boolean> sendDelayedOrderMessage(OrderStatusChangeDTO dto, int delayLevel) {
        try {
            Message<OrderStatusChangeDTO> message = MessageBuilder.withPayload(dto).build();
            // 发送延迟消息
            SendResult sendResult = rocketMQTemplate.syncSend("order-timeout-topic", message, 3000, delayLevel);
            
            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                log.info("延迟订单消息已成功投递至RocketMQ队列，消息ID: {}", sendResult.getMsgId());
            } else {
                log.warn("延迟订单消息投递至RocketMQ队列失败，状态: {}", sendResult.getSendStatus());
            }
        } catch (Exception e) {
            log.error("发送延迟订单消息失败", e);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }
    
    /**
     * 发送事务消息示例
     * @param dto 订单状态变更DTO
     * @return
     */
    public RestResult<Boolean> sendTransactionMessage(OrderStatusChangeDTO dto) {
        try {
            Message<OrderStatusChangeDTO> message = MessageBuilder.withPayload(dto).build();
            // 发送事务消息
            SendResult sendResult = rocketMQTemplate.sendMessageInTransaction("order-transaction-topic", 
                    message, null);
            
            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                log.info("事务消息已成功投递至RocketMQ队列，消息ID: {}", sendResult.getMsgId());
            } else {
                log.warn("事务消息投递至RocketMQ队列失败，状态: {}", sendResult.getSendStatus());
            }
        } catch (Exception e) {
            log.error("发送事务消息失败", e);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }

    /**
     * 判断订单状态是否合法
     *
     * @param status 订单状态
     * @return 是否合法
     */
    private boolean isValidOrderStatus(Integer status) {
        return status != null && (status >= 1 && status <= 4);
    }
    
    /**
     * 根据订单状态获取标签
     * @param status 订单状态
     * @return 标签
     */
    private String getStatusTag(Integer status) {
        switch (status) {
            case 1: return "created";
            case 2: return "paid";
            case 3: return "shipped";
            case 4: return "completed";
            default: return "unknown";
        }
    }
    
    /**
     * 根据操作类型获取标签
     * @param operationType 操作类型
     * @return 标签
     */
    private String getOperationTag(String operationType) {
        switch (operationType) {
            case "登录": return "login";
            case "下单": return "order";
            case "支付": return "payment";
            case "退款": return "refund";
            default: return "other";
        }
    }
}