package cn.sym.common.annotation;

/**
 * 敏感数据类型枚举
 */
public enum SensitiveType {
    /**
     * 无脱敏
     */
    NONE,

    /**
     * 价格脱敏（用于非授权用户查看）
     */
    PRICE,

    /**
     * 金额脱敏
     */
    AMOUNT,

    /**
     * 手机号脱敏
     */
    PHONE,

    /**
     * 邮箱脱敏
     */
    EMAIL
}

