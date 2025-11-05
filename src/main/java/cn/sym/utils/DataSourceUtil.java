package cn.sym.utils;

import cn.sym.config.DataSourceContextHolder;

/**
 * 数据源工具类
 */
public class DataSourceUtil {
    
    private static final ThreadLocal<Boolean> FORCE_MASTER = new ThreadLocal<>();
    
    /**
     * 设置强制使用主库
     */
    public static void forceMaster() {
        FORCE_MASTER.set(true);
    }
    
    /**
     * 清除强制使用主库标志
     */
    public static void clearForceMaster() {
        FORCE_MASTER.remove();
    }
    
    /**
     * 检查是否需要强制使用主库
     * @return 是否需要强制使用主库
     */
    public static boolean isForceMaster() {
        return FORCE_MASTER.get() != null && FORCE_MASTER.get();
    }
}
