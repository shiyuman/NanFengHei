package cn.sym.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class MultiLevelCacheConfig {

    /**
     * 本地缓存（Caffeine）配置
     * @return Cache
     */
    @Bean("localCache")
    public Cache<String, Object> localCache() {
        return Caffeine.newBuilder()
                // 设置过期时间
                .expireAfterWrite(10, TimeUnit.MINUTES)
                // 设置缓存最大条数
                .maximumSize(1000)
                // 设置缓存弱引用
                .weakValues()
                .build();
    }

    /**
     * Caffeine缓存管理器
     * @return CacheManager
     */
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(5000)
                .weakValues());
        return cacheManager;
    }

    /**
     * Redis缓存管理器
     * @param redisConnectionFactory Redis连接工厂
     * @return CacheManager
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置键的序列化方式
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置值的序列化方式
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                // 设置缓存过期时间
                .entryTtl(Duration.ofHours(1))
                // 禁用缓存空值（防止缓存穿透）
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}

