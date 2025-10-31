package cn.sym.repository;

import cn.sym.entity.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *   订单信息数据访问层接口
 * </p>
 * @author user
 */
@Repository
public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long> {
}