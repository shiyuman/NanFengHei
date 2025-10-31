package cn.sym.service;

import cn.sym.dto.OperationLogDTO;
import cn.sym.dto.OrderStatusChangeDTO;
import cn.sym.response.RestResult;

/**
 * 消息通知服务接口
 *
 * @author user
 */
public interface MessageNotificationService {

    /**
     * 发送订单状态变更通知
     *
     * @param dto 订单状态变更参数
     * @return 响应结果
     */
    RestResult<Boolean> sendOrderStatusChangeNotice(OrderStatusChangeDTO dto);

    /**
     * 记录操作日志
     *
     * @param dto 操作日志参数
     * @return 响应结果
     */
    RestResult<Boolean> recordOperationLog(OperationLogDTO dto);
}