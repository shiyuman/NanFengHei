package cn.sym.aspect;

import cn.sym.common.annotation.Idempotent;
import cn.sym.common.exception.IdempotentException;
import cn.sym.utils.IdempotentUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 幂等性切面
 */
@Slf4j
@Aspect
@Component
public class IdempotentAspect {

    @Autowired
    private IdempotentUtil idempotentUtil;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        // 检查并设置幂等性标识
        if (!idempotentUtil.checkAndSet(idempotent)) {
            // 幂等性检查失败
            log.warn("接口幂等性检查失败，拒绝重复请求");
            if (idempotent.throwException()) {
                throw new IdempotentException("请勿重复提交请求");
            } else {
                // 返回默认值或者自定义响应
                return null;
            }
        }

        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            return result;
        } finally {
            // TODO 清理幂等性标识（可选，根据业务需求决定是否需要立即清理）
            // idempotentUtil.remove(idempotent);
        }
    }
}
