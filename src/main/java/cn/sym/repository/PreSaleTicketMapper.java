package cn.sym.repository;

import cn.sym.entity.PreSaleTicketDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 早鸟票预售配置数据访问接口
 *
 * @author user
 */
@Mapper
public interface PreSaleTicketMapper extends BaseMapper<PreSaleTicketDO> {
}