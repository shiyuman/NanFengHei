package cn.sym.utils.convert;

import cn.sym.dto.OrderStatusChangeDTO;
import cn.sym.entity.OrderDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 订单相关对象转换工具类 (基于MapStruct)
 *
 * @author user
 */
@Mapper
public interface OrderConvert {
    
    OrderConvert INSTANCE = Mappers.getMapper(OrderConvert.class);

    /**
     * 将OrderStatusChangeDTO转换为OrderDO
     *
     * @param dto OrderStatusChangeDTO对象
     * @return OrderDO对象
     */
    @Mapping(source = "orderId", target = "id")
    OrderDO dto2DO(OrderStatusChangeDTO dto);
}
