package cn.sym.repository;

import cn.sym.entity.OrderInfo;
import java.util.List;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderInfoMapper {
    @Select("SELECT * FROM order_info WHERE id = #{id}")
    OrderInfo selectById(Long id);

    @Select("SELECT * FROM order_info WHERE user_id = #{userId}")
    List<OrderInfo> findByUserId(Long userId);
}