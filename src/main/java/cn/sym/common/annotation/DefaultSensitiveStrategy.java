package cn.sym.common.annotation;

import org.springframework.stereotype.Component;

/**
 * 默认脱敏策略实现
 */
@Component
public class DefaultSensitiveStrategy implements SensitiveStrategy {

    @Override
    public Object desensitize(Object original, SensitiveType sensitiveType) {
        if (original == null) {
            return null;
        }

        switch (sensitiveType) {
            case PRICE:
                // 价格脱敏：只显示整数部分，小数部分用*代替
                if (original instanceof Integer) {
                    return ((Integer) original) - (((Integer) original) % 100);
                } else if (original instanceof Long) {
                    return ((Long) original) - (((Long) original) % 100);
                }
                return "****";

            case AMOUNT:
                // 金额脱敏：显示大致范围
                if (original instanceof Number) {
                    Number number = (Number) original;
                    if (number.doubleValue() < 100) {
                        return "< 100";
                    } else if (number.doubleValue() < 1000) {
                        return "100 - 1000";
                    } else {
                        return "> 1000";
                    }
                }
                return "****";

            default:
                return original;
        }
    }
}

