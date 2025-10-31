package cn.sym.service.impl;

import cn.sym.dto.PreSaleTicketAddDTO;
import cn.sym.dto.PreSaleTicketEditDTO;
import cn.sym.dto.PreSaleTicketQueryDTO;
import cn.sym.entity.PreSaleTicketDO;
import cn.sym.entity.ProductInfoDO;
import cn.sym.exception.BusinessException;
import cn.sym.repository.PreSaleTicketMapper;
import cn.sym.repository.ProductInfoMapper;
import cn.sym.response.RestResult;
import cn.sym.response.ResultCodeConstant;
import cn.sym.service.PreSaleTicketService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import javax.annotation.Resource;
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
public class PreSaleTicketServiceImpl extends ServiceImpl<PreSaleTicketMapper, PreSaleTicketDO> implements PreSaleTicketService {

    @Resource
    private ProductInfoMapper productInfoMapper;

    @Resource
    private PreSaleTicketMapper preSaleTicketMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Boolean> addPreSaleTicket(PreSaleTicketAddDTO addDTO) {
        // 校验商品是否存在
        QueryWrapper<ProductInfoDO> productQuery = new QueryWrapper<>();
        productQuery.eq("id", addDTO.getProductId());
        ProductInfoDO product = productInfoMapper.selectOne(productQuery);
        if (product == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG, false);
        }

        // 校验预售时间段是否合法
        if (addDTO.getSaleStartTime().after(addDTO.getSaleEndTime())) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "预售时间不合法", false);
        }

        // 构造DO对象并保存
        PreSaleTicketDO preSaleTicketDO = new PreSaleTicketDO();
        BeanUtils.copyProperties(addDTO, preSaleTicketDO);
        preSaleTicketDO.setCreateBy("admin");
        preSaleTicketDO.setCreateTime(new Date());
        preSaleTicketDO.setUpdateBy("admin");
        preSaleTicketDO.setUpdateTime(new Date());

        boolean result = save(preSaleTicketDO);
        if (!result) {
            throw new BusinessException(ResultCodeConstant.CODE_000002, ResultCodeConstant.CODE_000002_MSG);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Boolean> editPreSaleTicket(PreSaleTicketEditDTO editDTO) {
        // 校验早鸟票预售配置是否存在
        PreSaleTicketDO existingPreSaleTicket = getById(editDTO.getId());
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
        preSaleTicketDO.setUpdateBy("admin");
        preSaleTicketDO.setUpdateTime(new Date());

        boolean result = updateById(preSaleTicketDO);
        if (!result) {
            throw new BusinessException(ResultCodeConstant.CODE_000002, ResultCodeConstant.CODE_000002_MSG);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    @Override
    public RestResult<PreSaleTicketDO> getPreSaleTicketDetail(PreSaleTicketQueryDTO queryDTO) {
        // 根据商品ID查询早鸟票预售配置信息
        QueryWrapper<PreSaleTicketDO> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", queryDTO.getProductId());
        PreSaleTicketDO preSaleTicketDO = preSaleTicketMapper.selectOne(wrapper);
        if (preSaleTicketDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "未找到该商品的早鸟票预售配置", null);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, preSaleTicketDO);
    }
}