package cn.sym.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等标识的前缀
     */
    String prefix() default "idempotent:";

    /**
     * 幂等标识的过期时间
     */
    long expireTime() default 3600;

    /**
     * 过期时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 幂等标识的获取方式
     */
    IdempotentKeyStrategy keyStrategy() default IdempotentKeyStrategy.REQUEST_ID;

    /**
     * 是否抛出异常
     */
    boolean throwException() default true;
}

