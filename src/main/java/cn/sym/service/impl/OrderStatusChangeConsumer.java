package cn.sym.service.impl;

import cn.sym.dto.OrderStatusChangeDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import cn.sym.repository.OrderMapper;
import cn.sym.entity.OrderDO;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RocketMQMessageListener(topic = "order-status-change-topic", 
                        consumerGroup = "order-status-change-consumer-group")
public class OrderStatusChangeConsumer implements RocketMQListener<OrderStatusChangeDTO> {
    
    private final OrderMapper orderMapper;
    
    private final StringRedisTemplate redisTemplate;
    
    private final SimpMessagingTemplate messagingTemplate;

    public OrderStatusChangeConsumer(OrderMapper orderMapper, StringRedisTemplate redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.orderMapper = orderMapper;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onMessage(OrderStatusChangeDTO message) {
        log.info("接收到订单状态变更消息: 订单ID={}, 订单编号={}, 状态={}, 消息={}", 
                message.getOrderId(), message.getOrderNo(), message.getStatus(), message.getMessage());
        
        // 在这里处理订单状态变更的业务逻辑
        // 例如：发送邮件通知、更新缓存、推送WebSocket消息等
        processOrderStatusChange(message);
    }
    
    private void processOrderStatusChange(OrderStatusChangeDTO message) {
        // 首先验证订单是否存在
        OrderDO order = orderMapper.selectById(message.getOrderId());
        if (order == null) {
            log.warn("订单不存在，订单ID: {}", message.getOrderId());
            return;
        }
        
        // 更新订单状态到数据库
        OrderDO updateOrder = new OrderDO();
        updateOrder.setId(message.getOrderId());
        updateOrder.setStatus(message.getStatus());
        orderMapper.updateById(updateOrder);
        
        // 更新缓存
        updateCache(message);
        
        // 推送WebSocket消息给前端
        pushWebSocketMessage(message);
        
        // 根据订单状态执行不同的处理逻辑
        switch (message.getStatus()) {
            case 1:
                log.info("订单已创建，订单ID: {}", message.getOrderId());
                // 发送邮件通知用户订单已创建
                sendEmailNotification(message, "订单创建通知", "您的订单已成功创建");
                // 设置订单超时未支付自动取消任务 (30分钟后)
                scheduleOrderCancelTask(message.getOrderId());
                break;
            case 2:
                log.info("订单已支付，订单ID: {}", message.getOrderId());
                // 更新库存、发送支付成功通知等
                updateInventory(message);
                sendEmailNotification(message, "支付成功通知", "您的订单已支付成功");
                // 清除订单超时任务
                cancelOrderCancelTask(message.getOrderId());
                break;
            case 3:
                log.info("订单已发货，订单ID: {}", message.getOrderId());
                // 发送物流信息通知
                sendShippingNotification(message);
                break;
            case 4:
                log.info("订单已完成，订单ID: {}", message.getOrderId());
                // 增加用户积分、发送评价邀请等
                addUserPoints(message);
                sendEmailNotification(message, "订单完成通知", "您的订单已完成，欢迎再次购买");
                break;
            case 5:
                log.info("订单已取消，订单ID: {}", message.getOrderId());
                // 恢复库存、处理退款等
                restoreInventory(message);
                refundProcess(message);
                break;
            default:
                log.warn("未知的订单状态: {}, 订单ID: {}", message.getStatus(), message.getOrderId());
        }
    }
    
    /**
     * 更新缓存中的订单状态
     */
    private void updateCache(OrderStatusChangeDTO message) {
        try {
            String cacheKey = "order_status_" + message.getOrderId();
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(message.getStatus()), 30, TimeUnit.MINUTES);
            log.info("订单状态已更新到缓存，订单ID: {}", message.getOrderId());
        } catch (Exception e) {
            log.error("更新订单状态缓存失败，订单ID: {}", message.getOrderId(), e);
        }
    }
    
    /**
     * 推送WebSocket消息给前端
     */
    private void pushWebSocketMessage(OrderStatusChangeDTO message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderId", message.getOrderId());
            payload.put("orderNo", message.getOrderNo());
            payload.put("status", message.getStatus());
            payload.put("message", message.getMessage());
            
            // 推送给特定用户的订单状态更新
            messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getOrderId()), 
                "/queue/order-status", 
                payload
            );
            
            // 广播给所有监听订单状态的客户端
            messagingTemplate.convertAndSend("/topic/order-status", payload);
            log.info("WebSocket消息已推送，订单ID: {}", message.getOrderId());
        } catch (Exception e) {
            log.error("推送WebSocket消息失败，订单ID: {}", message.getOrderId(), e);
        }
    }
    
    /**
     * 发送邮件通知
     */
    private void sendEmailNotification(OrderStatusChangeDTO message, String subject, String content) {
        try {
            // 这里应该是真实的邮件发送逻辑
            log.info("发送邮件通知: 主题={}, 内容={}, 订单ID={}", subject, content, message.getOrderId());
            // emailService.sendEmail(to, subject, content);
        } catch (Exception e) {
            log.error("发送邮件通知失败，订单ID: {}", message.getOrderId(), e);
        }
    }
    
    /**
     * 设置订单超时未支付自动取消任务
     */
    private void scheduleOrderCancelTask(Long orderId) {
        try {
            String key = "order_cancel_task_" + orderId;
            // 设置30分钟后自动取消订单的标记
            redisTemplate.opsForValue().set(key, "1", 30, TimeUnit.MINUTES);
            log.info("订单超时取消任务已设置，订单ID: {}", orderId);
        } catch (Exception e) {
            log.error("设置订单超时取消任务失败，订单ID: {}", orderId, e);
        }
    }
    
    /**
     * 取消订单超时未支付自动取消任务
     */
    private void cancelOrderCancelTask(Long orderId) {
        try {
            String key = "order_cancel_task_" + orderId;
            redisTemplate.delete(key);
            log.info("订单超时取消任务已取消，订单ID: {}", orderId);
        } catch (Exception e) {
            log.error("取消订单超时取消任务失败，订单ID: {}", orderId, e);
        }
    }
    
    /**
     * 更新库存
     */
    private void updateInventory(OrderStatusChangeDTO message) {
        try {
            // 这里应该是真实的库存更新逻辑
            log.info("库存已更新，订单ID: {}", message.getOrderId());
        } catch (Exception e) {
            log.error("更新库存失败，订单ID: {}", message.getOrderId(), e);
        }
    }
    
    /**
     * 发送发货通知
     */
    private void sendShippingNotification(OrderStatusChangeDTO message) {
        try {
            // 这里应该是真实的发货通知逻辑
            log.info("发货通知已发送，订单ID: {}", message.getOrderId());
        } catch (Exception e) {
            log.error("发送发货通知失败，订单ID: {}", message.getOrderId(), e);
        }
    }
    
    /**
     * 增加用户积分
     */
    private void addUserPoints(OrderStatusChangeDTO message) {
        try {
            // 这里应该是真实的用户积分增加逻辑
            log.info("用户积分已增加，订单ID: {}", message.getOrderId());
        } catch (Exception e) {
            log.error("增加用户积分失败，订单ID: {}", message.getOrderId(), e);
        }
    }
    
    /**
     * 恢复库存
     */
    private void restoreInventory(OrderStatusChangeDTO message) {
        try {
            // 这里应该是真实的库存恢复逻辑
            log.info("库存已恢复，订单ID: {}", message.getOrderId());
        } catch (Exception e) {
            log.error("恢复库存失败，订单ID: {}", message.getOrderId(), e);
        }
    }
    
    /**
     * 处理退款
     */
    private void refundProcess(OrderStatusChangeDTO message) {
        try {
            // 这里应该是真实的退款处理逻辑
            log.info("退款流程已启动，订单ID: {}", message.getOrderId());
        } catch (Exception e) {
            log.error("处理退款失败，订单ID: {}", message.getOrderId(), e);
        }
    }
}

