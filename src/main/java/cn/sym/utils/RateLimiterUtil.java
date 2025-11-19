package cn.sym.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.scripting.support.ResourceScriptSource;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RateLimiterUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    private DefaultRedisScript<Long> rateLimitScript;

    @PostConstruct
    public void init() {
        rateLimitScript = new DefaultRedisScript<>();
        rateLimitScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/token_bucket.lua")));
        rateLimitScript.setResultType(Long.class);
    }

    /**
     * 执行限流检查
     *
     * @param key      限流标识key
     * @param capacity 桶容量
     * @param rate     令牌生成速率(每秒)
     * @param requested 当前请求需要的令牌数
     * @return true表示允许访问，false表示拒绝访问
     */
    public boolean isAllowed(String key, int capacity, int rate, int requested) {
        long now = Instant.now().getEpochSecond();
        List<String> keys = Arrays.asList(key);
        Long result = redisTemplate.execute(rateLimitScript, keys, String.valueOf(capacity),
                                           String.valueOf(rate), String.valueOf(requested), String.valueOf(now));
        return result != null && result == 1;
    }

    /**
     * 简化版限流检查，默认每次请求消耗1个令牌
     *
     * @param key      限流标识key
     * @param capacity 桶容量
     * @param rate     令牌生成速率(每秒)
     * @return true表示允许访问，false表示拒绝访问
     */
    public boolean isAllowed(String key, int capacity, int rate) {
        return isAllowed(key, capacity, rate, 1);
    }
}
