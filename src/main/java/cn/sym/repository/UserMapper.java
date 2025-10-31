package cn.sym.repository;

import cn.sym.entity.UserDO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Repository
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    @Select("SELECT * FROM user_info WHERE id = #{id}")
    UserDO selectById(Long id);

    @Select("SELECT * FROM user_info WHERE username = #{username}")
    UserDO selectByUsername(@Param("username") String username);

    @Delete("DELETE FROM user_info WHERE id = #{id}")
    int deleteById(Long id);

    @Insert("INSERT INTO user_info(username, password, phone, status, create_by, create_time, update_by, update_time) " +
            "VALUES(#{username}, #{password}, #{phone}, #{status}, #{createBy}, #{createTime}, #{updateBy}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserDO userDO);

    @Update("UPDATE user_info SET username=#{username}, password=#{password}, phone=#{phone}, status=#{status}, " +
            "update_by=#{updateBy}, update_time=#{updateTime} WHERE id=#{id}")
    int updateById(UserDO userDO);

    // 用于分页查询
    <P extends Page<UserDO>> P selectPage(P page, @Param("ew") Wrapper<UserDO> wrapper);
}
