package cn.sym.service;

import cn.sym.entity.ProductDO;
import cn.sym.repository.ProductMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiLevelCacheService {


    private final ProductMapper productMapper;

    //Lombok 不复制 @Qualifier 注解
//    @Qualifier("localCache")
    private final Cache<String, Object> localCache;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    // 用于防止缓存击穿的分布式锁
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /**
     * 获取商品详情（多级缓存）
     * @param productId 商品ID
     * @return 商品信息
     */
    public ProductDO getProductDetailWithCache(Long productId) {
        if (productId == null) {
            return null;
        }

        String cacheKey = "product:detail:" + productId;

        // 1. 先从本地缓存获取
        ProductDO product = (ProductDO) localCache.getIfPresent(cacheKey);
        if (product != null) {
            log.debug("从本地缓存获取商品信息，商品ID: {}", productId);
            return product;
        }

        // 2. 从Redis缓存获取
        String productJson = redisTemplate.opsForValue().get(cacheKey);
        if (productJson != null) {
            try {
                product = objectMapper.readValue(productJson, ProductDO.class);
                // 回种到本地缓存
                localCache.put(cacheKey, product);
                log.debug("从Redis缓存获取商品信息并回种到本地缓存，商品ID: {}", productId);
                return product;
            } catch (Exception e) {
                log.error("解析Redis缓存中的商品信息失败，商品ID: {}", productId, e);
            }
        }

        // 3. 防止缓存击穿，使用分布式锁
        Lock lock = lockMap.computeIfAbsent(cacheKey, k -> new ReentrantLock());
        lock.lock();
        try {
            // 双重检查
            product = (ProductDO) localCache.getIfPresent(cacheKey);
            if (product != null) {
                return product;
            }

            String redisValue = redisTemplate.opsForValue().get(cacheKey);
            if (redisValue != null) {
                try {
                    product = objectMapper.readValue(redisValue, ProductDO.class);
                    localCache.put(cacheKey, product);
                    return product;
                } catch (Exception e) {
                    log.error("解析Redis缓存中的商品信息失败，商品ID: {}", productId, e);
                }
            }

            // 4. 从数据库查询
            product = productMapper.selectById(productId);
            if (product != null) {
                // 写入各级缓存
                try {
                    String json = objectMapper.writeValueAsString(product);
                    // 写入Redis缓存（带过期时间）
                    redisTemplate.opsForValue().set(cacheKey, json);
                    // 写入本地缓存
                    localCache.put(cacheKey, product);
                } catch (Exception e) {
                    log.error("商品信息写入缓存失败，商品ID: {}", productId, e);
                }
            } else {
                // 防止缓存穿透，缓存空值（设置较短的过期时间）
                redisTemplate.opsForValue().set(cacheKey, "", java.time.Duration.ofMinutes(5));
            }

            return product;
        } finally {
            lock.unlock();
            lockMap.remove(cacheKey);
        }
    }

    /**
     * 更新商品信息并清除缓存
     * @param productDO 商品信息
     */
    public void updateProductAndClearCache(ProductDO productDO) {
        if (productDO == null || productDO.getId() == null) {
            return;
        }

        String cacheKey = "product:detail:" + productDO.getId();

        // 先更新数据库
        productMapper.updateById(productDO);

        // 清除各级缓存
        localCache.invalidate(cacheKey);
        redisTemplate.delete(cacheKey);
    }

    /**
     * 删除商品并清除缓存
     * @param productId 商品ID
     */
    public void deleteProductAndClearCache(Long productId) {
        if (productId == null) {
            return;
        }

        String cacheKey = "product:detail:" + productId;

        // 先删除数据库记录
        productMapper.deleteById(productId);

        // 清除各级缓存
        localCache.invalidate(cacheKey);
        redisTemplate.delete(cacheKey);
    }

    /**
     * 预热商品缓存
     * @param productId 商品ID
     */
    public void warmUpProductCache(Long productId) {
        if (productId == null) {
            return;
        }

        try {
            ProductDO product = productMapper.selectById(productId);
            if (product != null) {
                String cacheKey = "product:detail:" + productId;
                String json = objectMapper.writeValueAsString(product);

                // 预热到Redis缓存
                redisTemplate.opsForValue().set(cacheKey, json);
                // 预热到本地缓存
                localCache.put(cacheKey, product);

                log.info("商品缓存预热成功，商品ID: {}", productId);
            }
        } catch (Exception e) {
            log.error("商品缓存预热失败，商品ID: {}", productId, e);
        }
    }
}

