package cn.sym.service.impl;

import cn.sym.dto.LimitPurchaseAddDTO;
import cn.sym.dto.LimitPurchaseEditDTO;
import cn.sym.dto.LimitPurchaseQueryDTO;
import cn.sym.entity.LimitPurchaseDO;
import cn.sym.entity.ProductDO;
import cn.sym.common.exception.BusinessException;
import cn.sym.repository.LimitPurchaseMapper;
import cn.sym.repository.ProductMapper;
import cn.sym.common.response.RestResult;
import cn.sym.common.response.ResultCodeConstant;
import cn.sym.service.LimitPurchaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 限购配置服务实现类
 *
 * @author user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LimitPurchaseServiceImpl extends ServiceImpl<LimitPurchaseMapper, LimitPurchaseDO> implements LimitPurchaseService {
    private final ProductMapper productMapper;

    private final LimitPurchaseMapper limitPurchaseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Boolean> addLimitPurchase(LimitPurchaseAddDTO addDTO) {
        // 校验商品是否存在
        LambdaQueryWrapper<ProductDO> productQuery = new LambdaQueryWrapper<>();
        productQuery.eq(ProductDO::getId, addDTO.getProductId());
        ProductDO product = productMapper.selectOne(productQuery);
        if (product == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG, false);
        }

        LimitPurchaseDO limitPurchaseDO = new LimitPurchaseDO();
        BeanUtils.copyProperties(addDTO, limitPurchaseDO);

        boolean result = save(limitPurchaseDO);
        if (!result) {
            throw new BusinessException(ResultCodeConstant.CODE_000002, ResultCodeConstant.CODE_000002_MSG);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Boolean> editLimitPurchase(LimitPurchaseEditDTO editDTO) {
        // 校验限购配置是否存在
        LimitPurchaseDO existingLimitPurchase = getById(editDTO.getId());
        if (existingLimitPurchase == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "限购配置不存在", false);
        }

        // 校验最大购买数量
        if (editDTO.getMaxQuantity() <= 0) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "最大购买数量必须大于0", false);
        }

        // 更新限购配置信息
        LimitPurchaseDO limitPurchaseDO = new LimitPurchaseDO();
        BeanUtils.copyProperties(editDTO, limitPurchaseDO);
        limitPurchaseDO.setId(editDTO.getId());

        boolean result = updateById(limitPurchaseDO);
        if (!result) {
            throw new BusinessException(ResultCodeConstant.CODE_000002, ResultCodeConstant.CODE_000002_MSG);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    @Override
    public RestResult<LimitPurchaseDO> getLimitPurchaseDetail(LimitPurchaseQueryDTO queryDTO) {
        // 根据商品ID查询限购配置信息
        LambdaQueryWrapper<LimitPurchaseDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LimitPurchaseDO::getProductId, queryDTO.getProductId());
        LimitPurchaseDO limitPurchaseDO = limitPurchaseMapper.selectOne(wrapper);
        if (limitPurchaseDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "未找到该商品的限购配置", null);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, limitPurchaseDO);
    }
}