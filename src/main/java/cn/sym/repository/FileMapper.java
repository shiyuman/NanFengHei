package cn.sym.repository;

import cn.sym.entity.FileDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件存储数据访问接口
 *
 * @author user
 */
@Mapper
public interface FileMapper extends BaseMapper<FileDO> {
}