package cn.sym.config;

import cn.sym.utils.DataSourceUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据源切换切面
 */
@Aspect
@Order(1)
@Component
public class DataSourceAspect {
    
    /**
     * 定义切点
     * 匹配所有service包下的方法
     */
    @Pointcut("execution(* cn.sym.service..*.*(..))")
    public void dataSourcePointCut() {
        
    }
    
    /**
     * 环绕通知
     * @param point 切点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        String dataSource = DataSourceContextHolder.MASTER;
        
        // 获取方法上的注解
        DataSource dataSourceAnnotation = method.getAnnotation(DataSource.class);
        if (dataSourceAnnotation != null) {
            dataSource = dataSourceAnnotation.value();
        } else {
            // 如果方法上没有注解，检查类上的注解
            Class<?> targetClass = point.getTarget().getClass();
            DataSource classAnnotation = targetClass.getAnnotation(DataSource.class);
            if (classAnnotation != null) {
                dataSource = classAnnotation.value();
            }
        }
        
        // 设置数据源
        DataSourceContextHolder.setDataSourceType(dataSource);
        
        try {
            // 执行方法
            return point.proceed();
        } finally {
            // 清除数据源设置
            DataSourceContextHolder.clearDataSourceType();
            // 清除强制使用主库标志
            DataSourceUtil.clearForceMaster();
        }
    }
}
