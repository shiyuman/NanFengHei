package cn.sym.common.annotation;

/**
 * 幂等标识获取策略
 */
public enum IdempotentKeyStrategy {
    /**
     * 从请求参数中获取requestId
     */
    REQUEST_ID,

    /**
     * 从请求头中获取
     */
    HEADER,

    /**
     * 从Token中获取用户ID
     */
    TOKEN_USER_ID,

    /**
     * 从请求URL中获取
     */
    URL,

    /**
     * 自定义策略
     */
    CUSTOM
}

