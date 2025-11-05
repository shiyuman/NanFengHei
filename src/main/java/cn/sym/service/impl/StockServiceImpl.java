package cn.sym.service.impl;

import cn.sym.dto.OrderProductDTO;
import cn.sym.dto.StockDeductionMessage;
import cn.sym.entity.ProductDO;
import cn.sym.repository.ProductMapper;
import cn.sym.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 库存服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "STOCK_DEDUCTION_TOPIC", consumerGroup = "STOCK-DEDUCTION-CONSUMER")
public class StockServiceImpl implements RocketMQListener<StockDeductionMessage>, StockService {

    private final ProductMapper productMapper;

    /**
     * 监听库存扣减消息
     * @param message 库存扣减消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(StockDeductionMessage message) {
        log.info("接收到库存扣减消息，订单号: {}", message.getOrderNo());

        // 用于记录已扣减库存的商品，以便在出现异常时回滚
        Map<Long, Integer> deductedProductMap = new HashMap<>();

        try {
            // 扣减商品库存
            for (OrderProductDTO item : message.getProductList()) {
                // 查询商品信息获取当前版本号
                ProductDO product = productMapper.selectById(item.getProductId());
                if (product == null) {
                    throw new RuntimeException("商品[" + item.getProductId() + "]不存在");
                }

                // 检查库存是否充足
                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("商品[" + item.getProductId() + "]库存不足，当前库存: " + product.getStock() + "，需扣减: " + item.getQuantity());
                }

                // 使用乐观锁扣减库存
                int updateCount = productMapper.deductStockWithVersion(
                        item.getProductId(),
                        item.getQuantity(),
                        product.getVersion());

                if (updateCount <= 0) {
                    throw new RuntimeException("商品[" + item.getProductId() + "]库存扣减失败，可能由于并发更新导致版本号不匹配");
                }

                // 记录已扣减库存的商品
                deductedProductMap.put(item.getProductId(), item.getQuantity());

                log.info("商品[{}]库存扣减成功，扣减数量: {}", item.getProductId(), item.getQuantity());
            }

            log.info("订单[{}]库存扣减全部完成", message.getOrderNo());

        } catch (Exception e) {
            log.error("库存扣减失败，订单号: {}，错误信息: {}", message.getOrderNo(), e.getMessage(), e);

            // 回滚已扣减的库存
            rollbackDeductedStock(deductedProductMap);

            // 重新抛出异常，让RocketMQ重新投递消息
            throw new RuntimeException("库存扣减失败，消息将重新投递", e);
        }
    }

    /**
     * 回滚已扣减的库存
     * @param deductedProductMap 已扣减库存的商品ID和数量映射
     */
    private void rollbackDeductedStock(Map<Long, Integer> deductedProductMap) {
        for (Map.Entry<Long, Integer> entry : deductedProductMap.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            try {
                // 查询当前商品信息，获取最新的版本号
                ProductDO product = productMapper.selectById(productId);
                if (product != null) {
                    // 使用乐观锁恢复库存，传入最新的版本号
                    int updateCount = productMapper.increaseStockWithVersion(productId, quantity, product.getVersion());
                    if (updateCount <= 0) {
                        log.warn("恢复商品[{}]库存失败，可能由于并发更新导致版本号不匹配，需要人工干预", productId);
                    } else {
                        log.info("成功恢复商品[{}]库存{}件", productId, quantity);
                    }
                } else {
                    log.warn("回滚库存时未找到商品[{}]，可能已被删除", productId);
                }
            } catch (Exception ex) {
                log.error("回滚商品[{}]库存失败", productId, ex);
            }
        }
    }
}
