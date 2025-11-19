package cn.sym.service;

import cn.sym.dto.LimitPurchaseAddDTO;
import cn.sym.dto.LimitPurchaseEditDTO;
import cn.sym.dto.LimitPurchaseQueryDTO;
import cn.sym.entity.LimitPurchaseDO;
import cn.sym.common.response.RestResult;

/**
 * 限购配置服务接口
 *
 * @author user
 */
public interface LimitPurchaseService {

    /**
     * 新增限购配置
     *
     * @param addDTO 新增参数对象
     * @return RestResult 结果
     */
    RestResult<Boolean> addLimitPurchase(LimitPurchaseAddDTO addDTO);

    /**
     * 编辑限购配置
     *
     * @param editDTO 编辑参数对象
     * @return RestResult 结果
     */
    RestResult<Boolean> editLimitPurchase(LimitPurchaseEditDTO editDTO);

    /**
     * 查询限购配置详情
     *
     * @param queryDTO 查询参数对象
     * @return RestResult 结果
     */
    RestResult<LimitPurchaseDO> getLimitPurchaseDetail(LimitPurchaseQueryDTO queryDTO);
}