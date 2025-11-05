package cn.sym.config;

import java.lang.annotation.*;

/**
 * 数据源注解
 * 用于标记方法使用主库还是从库
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

    String value() default DataSourceContextHolder.MASTER;
}

