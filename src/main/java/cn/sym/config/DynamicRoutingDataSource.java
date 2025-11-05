package cn.sym.config;

import cn.sym.utils.DataSourceUtil;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源路由类
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        // 检查是否需要强制使用主库
        if (DataSourceUtil.isForceMaster()) {
            return DataSourceContextHolder.MASTER;
        }

        return DataSourceContextHolder.getDataSourceType();
    }
}
