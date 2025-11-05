package cn.sym.service;

import cn.sym.dto.StockDeductionMessage;

/**
 * 库存服务接口
 */
public interface StockService {
    /**
     * 处理库存扣减消息
     * @param message 库存扣减消息
     */
    void onMessage(StockDeductionMessage message);
}

