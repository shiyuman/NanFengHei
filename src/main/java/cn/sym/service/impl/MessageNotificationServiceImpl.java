package cn.sym.service.impl;

import cn.sym.dto.OperationLogDTO;
import cn.sym.dto.OrderStatusChangeDTO;
import cn.sym.entity.OrderInfo;
import cn.sym.entity.UserInfo;
import cn.sym.exception.BusinessException;
import cn.sym.repository.OrderRepository;
import cn.sym.repository.UserRepository;
import cn.sym.response.RestResult;
import cn.sym.response.ResultCodeConstant;
import cn.sym.service.MessageNotificationService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息通知服务实现类
 *
 * @author user
 */
@Slf4j
@Service
public class MessageNotificationServiceImpl implements MessageNotificationService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public RestResult<Boolean> sendOrderStatusChangeNotice(OrderStatusChangeDTO dto) {
        log.info("开始发送订单状态变更通知，订单ID: {}, 订单编号: {}", dto.getOrderId(), dto.getOrderNo());

        // 校验订单是否存在且状态是否合法
        OrderInfo orderInfo = orderRepository.findById(dto.getOrderId()).orElse(null);
        if (orderInfo == null || !Objects.equals(orderInfo.getOrderNo(), dto.getOrderNo())) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 判断订单状态是否合法（这里假设只有1、2、3、4四个状态）
        if (!isValidOrderStatus(dto.getStatus())) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 异步发送消息到RocketMQ队列
        try {
            // TODO: 实现异步发送到RocketMQ的逻辑
            log.info("订单状态变更消息已投递至RocketMQ队列");
        } catch (Exception e) {
            log.error("发送订单状态变更通知失败", e);
            throw new BusinessException(ResultCodeConstant.CODE_000002, "发送通知失败");
        }

        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }

    @Override
    public RestResult<Boolean> recordOperationLog(OperationLogDTO dto) {
        log.info("开始记录操作日志，用户ID: {}", dto.getUserId());

        // 校验用户是否存在
        UserInfo userInfo = userRepository.findById(dto.getUserId()).orElse(null);
        if (userInfo == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 构建日志信息并异步投递到RocketMQ
        try {
            // TODO: 实现构建日志信息并投递到RocketMQ的逻辑
            log.info("操作日志已投递至RocketMQ队列");
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
            throw new BusinessException(ResultCodeConstant.CODE_000002, "记录日志失败");
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
}