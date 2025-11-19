package cn.sym.service.impl;

import cn.sym.dto.PreSaleTicketAddDTO;
import cn.sym.dto.PreSaleTicketEditDTO;
import cn.sym.dto.PreSaleTicketQueryDTO;
import cn.sym.entity.PreSaleTicketDO;
import cn.sym.entity.ProductDO;
import cn.sym.common.exception.BusinessException;
import cn.sym.repository.PreSaleTicketMapper;
import cn.sym.repository.ProductMapper;
import cn.sym.common.response.RestResult;
import cn.sym.common.response.ResultCodeConstant;
import cn.sym.service.PreSaleTicketService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 早鸟票预售配置服务实现类
 *
 * @author user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreSaleTicketServiceImpl implements PreSaleTicketService {

    private final ProductMapper productMapper;

    private final PreSaleTicketMapper preSaleTicketMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Boolean> addPreSaleTicket(PreSaleTicketAddDTO addDTO) {
        // 校验商品是否存在
        LambdaQueryWrapper<ProductDO> productQuery = new LambdaQueryWrapper<>();
        productQuery.eq(ProductDO::getId, addDTO.getProductId());
        ProductDO product = productMapper.selectOne(productQuery);
        if (product == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG, false);
        }

        // 校验预售时间段是否合法
        if (addDTO.getSaleStartTime().after(addDTO.getSaleEndTime())) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "预售时间不合法", false);
        }

        PreSaleTicketDO preSaleTicketDO = new PreSaleTicketDO();
        BeanUtils.copyProperties(addDTO, preSaleTicketDO);

        boolean result = preSaleTicketMapper.insert(preSaleTicketDO) > 0;
        if (!result) {
            throw new BusinessException(ResultCodeConstant.CODE_000002, ResultCodeConstant.CODE_000002_MSG);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Boolean> editPreSaleTicket(PreSaleTicketEditDTO editDTO) {
        // 校验早鸟票预售配置是否存在
        PreSaleTicketDO existingPreSaleTicket = preSaleTicketMapper.selectById(editDTO.getId());
        if (existingPreSaleTicket == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "早鸟票配置不存在", false);
        }

        // 校验预售时间段是否合法
        if (editDTO.getSaleStartTime().after(editDTO.getSaleEndTime())) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "预售时间不合法", false);
        }

        // 更新早鸟票预售配置信息
        PreSaleTicketDO preSaleTicketDO = new PreSaleTicketDO();
        BeanUtils.copyProperties(editDTO, preSaleTicketDO);
        preSaleTicketDO.setId(editDTO.getId());

        boolean result = preSaleTicketMapper.updateById(preSaleTicketDO) > 0;
        if (!result) {
            throw new BusinessException(ResultCodeConstant.CODE_000002, ResultCodeConstant.CODE_000002_MSG);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    @Override
    public RestResult<PreSaleTicketDO> getPreSaleTicketDetail(PreSaleTicketQueryDTO queryDTO) {
        // 根据商品ID查询早鸟票预售配置信息
        LambdaQueryWrapper<PreSaleTicketDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PreSaleTicketDO::getProductId, queryDTO.getProductId());
        PreSaleTicketDO preSaleTicketDO = preSaleTicketMapper.selectOne(wrapper);
        if (preSaleTicketDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "未找到该商品的早鸟票预售配置", null);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, preSaleTicketDO);
    }
}