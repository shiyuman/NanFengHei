package cn.sym.service.impl;

import cn.sym.dto.LimitPurchaseAddDTO;
import cn.sym.dto.LimitPurchaseEditDTO;
import cn.sym.dto.LimitPurchaseQueryDTO;
import cn.sym.entity.LimitPurchaseDO;
import cn.sym.entity.ProductInfoDO;
import cn.sym.exception.BusinessException;
import cn.sym.repository.LimitPurchaseMapper;
import cn.sym.repository.ProductInfoMapper;
import cn.sym.response.RestResult;
import cn.sym.response.ResultCodeConstant;
import cn.sym.service.LimitPurchaseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import javax.annotation.Resource;
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
public class LimitPurchaseServiceImpl extends ServiceImpl<LimitPurchaseMapper, LimitPurchaseDO> implements LimitPurchaseService {

    @Resource
    private ProductInfoMapper productInfoMapper;

    @Resource
    private LimitPurchaseMapper limitPurchaseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Boolean> addLimitPurchase(LimitPurchaseAddDTO addDTO) {
        // 校验商品是否存在
        QueryWrapper<ProductInfoDO> productQuery = new QueryWrapper<>();
        productQuery.eq("id", addDTO.getProductId());
        ProductInfoDO product = productInfoMapper.selectOne(productQuery);
        if (product == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG, false);
        }

        // 构造DO对象并保存
        LimitPurchaseDO limitPurchaseDO = new LimitPurchaseDO();
        BeanUtils.copyProperties(addDTO, limitPurchaseDO);
        limitPurchaseDO.setCreateBy("admin");
        limitPurchaseDO.setCreateTime(new Date());
        limitPurchaseDO.setUpdateBy("admin");
        limitPurchaseDO.setUpdateTime(new Date());

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

        // 更新限购配置信息
        LimitPurchaseDO limitPurchaseDO = new LimitPurchaseDO();
        BeanUtils.copyProperties(editDTO, limitPurchaseDO);
        limitPurchaseDO.setId(editDTO.getId());
        limitPurchaseDO.setUpdateBy("admin");
        limitPurchaseDO.setUpdateTime(new Date());

        boolean result = updateById(limitPurchaseDO);
        if (!result) {
            throw new BusinessException(ResultCodeConstant.CODE_000002, ResultCodeConstant.CODE_000002_MSG);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    @Override
    public RestResult<LimitPurchaseDO> getLimitPurchaseDetail(LimitPurchaseQueryDTO queryDTO) {
        // 根据商品ID查询限购配置信息
        QueryWrapper<LimitPurchaseDO> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", queryDTO.getProductId());
        LimitPurchaseDO limitPurchaseDO = limitPurchaseMapper.selectOne(wrapper);
        if (limitPurchaseDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "未找到该商品的限购配置", null);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, limitPurchaseDO);
    }
}