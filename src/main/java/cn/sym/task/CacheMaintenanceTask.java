package cn.sym.task;

import cn.sym.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheMaintenanceTask {

    @Autowired
    private ProductService productService;

    /**
     * 定时预热热门商品缓存（每天凌晨2点执行）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void warmUpHotProductCache() {
        log.info("开始预热热门商品缓存");

        // 这里应该从数据库或者配置文件中获取热门商品ID列表
        // 示例：预热商品ID为1,2,3的商品
        Long[] hotProductIds = {1L, 2L, 3L};

        for (Long productId : hotProductIds) {
            try {
                productService.warmUpProductCache(productId);
                Thread.sleep(100); // 避免并发过高
            } catch (Exception e) {
                log.error("预热商品缓存失败，商品ID: {}", productId, e);
            }
        }

        log.info("热门商品缓存预热完成");
    }

    /**
     * 定时清理过期缓存（每小时执行一次）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredCache() {
        log.info("开始清理过期缓存");
        // Redis会自动清理过期缓存，这里可以做一些额外的清理工作
        log.info("过期缓存清理完成");
    }
}

