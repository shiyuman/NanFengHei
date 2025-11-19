package cn.sym.service;

import cn.sym.dto.PreSaleTicketAddDTO;
import cn.sym.dto.PreSaleTicketEditDTO;
import cn.sym.dto.PreSaleTicketQueryDTO;
import cn.sym.entity.PreSaleTicketDO;
import cn.sym.common.response.RestResult;

/**
 * 早鸟票预售配置服务接口
 *
 * @author user
 */
public interface PreSaleTicketService {

    /**
     * 新增早鸟票预售配置
     *
     * @param addDTO 新增参数对象
     * @return RestResult 结果
     */
    RestResult<Boolean> addPreSaleTicket(PreSaleTicketAddDTO addDTO);

    /**
     * 编辑早鸟票预售配置
     *
     * @param editDTO 编辑参数对象
     * @return RestResult 结果
     */
    RestResult<Boolean> editPreSaleTicket(PreSaleTicketEditDTO editDTO);

    /**
     * 查询早鸟票预售配置详情
     *
     * @param queryDTO 查询参数对象
     * @return RestResult 结果
     */
    RestResult<PreSaleTicketDO> getPreSaleTicketDetail(PreSaleTicketQueryDTO queryDTO);
}