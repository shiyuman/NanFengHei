package cn.sym.utils;

import cn.sym.common.annotation.SensitiveData;
import cn.sym.common.annotation.SensitiveStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;

/**
 * 数据脱敏工具类
 */
@Component
public class DataDesensitizeUtil {

    @Autowired
    private static SensitiveStrategy sensitiveStrategy;

    /**
     * 对对象进行脱敏处理
     * @param source 原始对象
     * @param <T> 对象类型
     * @return 脱敏后的对象
     */
    public static <T> T desensitize(T source) {
        if (source == null) {
            return null;
        }

        try {
            // 创建新的实例
            @SuppressWarnings("unchecked")
            T target = (T) source.getClass().getDeclaredConstructor().newInstance();

            // 获取所有字段
            Field[] fields = source.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);

                // 检查是否有敏感数据注解
                SensitiveData sensitiveData = field.getAnnotation(SensitiveData.class);
                if (sensitiveData != null) {
                    Object originalValue = field.get(source);
                    Object desensitizedValue = sensitiveStrategy.desensitize(originalValue, sensitiveData.value());
                    field.set(target, desensitizedValue);
                } else {
                    // 非敏感字段直接复制
                    Object value = field.get(source);
                    field.set(target, value);
                }
            }

            return target;
        } catch (Exception e) {
            // 出现异常时返回原始对象
            return source;
        }
    }
}

