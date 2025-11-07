package cn.sym.common.annotation;

/**
 * 敏感数据脱敏策略接口
 */
public interface SensitiveStrategy {
    /**
     * 脱敏处理
     * @param original 原始数据
     * @param sensitiveType 敏感数据类型
     * @return 脱敏后的数据
     */
    Object desensitize(Object original, SensitiveType sensitiveType);
}

