package cn.sym.utils;

import cn.sym.common.annotation.Idempotent;
import cn.sym.common.exception.IdempotentException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 幂等性工具类
 */
@Component
@RequiredArgsConstructor
public class IdempotentUtil {


    private final StringRedisTemplate redisTemplate;

    /**
     * 检查并设置幂等性标识
     * @param idempotent 幂等性注解
     * @return 是否可以继续执行
     */
    public boolean checkAndSet(Idempotent idempotent) {
        String key = generateKey(idempotent);
        if (StringUtils.isEmpty(key)) {
            throw new IdempotentException("无法生成幂等性标识");
        }

        String redisKey = idempotent.prefix() + key;

        // 使用Redis的SET命令的NX选项实现原子性操作
        Boolean result = redisTemplate.opsForValue().setIfAbsent(
            redisKey,
            "1",
            idempotent.expireTime(),
            idempotent.timeUnit()
        );

        return result != null && result;
    }

    /**
     * 生成幂等性标识
     * @param idempotent 幂等性注解
     * @return 幂等性标识
     */
    private String generateKey(Idempotent idempotent) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        switch (idempotent.keyStrategy()) {
            case REQUEST_ID:
                // 从请求参数中获取requestId
                String requestId = request.getParameter("requestId");
                if (StringUtils.isEmpty(requestId)) {
                    // 从请求体中获取requestId（需要根据实际情况实现）
                    requestId = getRequestRequestId(request);
                }
                return requestId;

            case HEADER:
                // 从请求头中获取
                return request.getHeader("Idempotent-Key");

            case TOKEN_USER_ID:
                // 从Token中获取用户ID（需要根据实际情况实现）
                return getUserIdFromToken(request);

            case URL:
                // 从请求URL中获取
                return request.getRequestURI();

            case CUSTOM:
            default:
                return null;
        }
    }

    /**
     * 获取当前请求
     * @return HttpServletRequest
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 从请求中获取requestId（需要根据实际情况实现）
     * @param request 请求
     * @return requestId
     */
    private String getRequestRequestId(HttpServletRequest request) {
        // 这里需要根据实际的请求体解析方式来实现
        // 比如从JSON请求体中解析requestId字段
        return request.getParameter("requestId");
    }

    /**
     * 从Token中获取用户ID（需要根据实际情况实现）
     * @param request 请求
     * @return 用户ID
     */
    private String getUserIdFromToken(HttpServletRequest request) {
        // 这里需要根据实际的Token解析方式来实现
        // 比如解析JWT Token获取用户ID
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        // 解析Token获取用户ID的逻辑
        return "userId";
    }

    /**
     * 删除幂等性标识
     * @param idempotent 幂等性注解
     */
    public void remove(Idempotent idempotent) {
        String key = generateKey(idempotent);
        if (!StringUtils.isEmpty(key)) {
            String redisKey = idempotent.prefix() + key;
            redisTemplate.delete(redisKey);
        }
    }
}
