package cn.sym.repository;

import cn.sym.entity.OrderDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cn.sym.entity.OrderInfo;

/**
 * 订单数据访问接口
 *
 * @author user
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderDO, Long> {

    /**
     * 根据订单编号查找订单
     * @param orderNo 订单编号
     * @return 订单对象
     */
    OrderDO findByOrderNo(String orderNo);
}
