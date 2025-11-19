package cn.sym.service.impl;

import cn.sym.dto.CouponDTO;
import cn.sym.dto.CouponQuery;
import cn.sym.entity.CouponDO;
import cn.sym.common.exception.BusinessException;
import cn.sym.repository.CouponMapper;
import cn.sym.common.response.ResultCodeConstant;
import cn.sym.service.CouponService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 *   优惠券服务实现类
 * </p>
 * @author user
 */
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponDO> implements CouponService {

    @Override
    public Boolean addCoupon(CouponDTO couponDTO) {
        // 校验输入合法性
        if (couponDTO.getName() == null || couponDTO.getType() == null ||
                couponDTO.getDiscountAmount() == null || couponDTO.getMinAmount() == null ||
                couponDTO.getStartTime() == null || couponDTO.getEndTime() == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "优惠券信息不完整");
        }

        // 校验时间有效性
        if (couponDTO.getStartTime().after(couponDTO.getEndTime())) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "优惠券开始时间不能晚于结束时间");
        }

        // 检查是否存在相同名称的优惠券
        LambdaQueryWrapper<CouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponDO::getName, couponDTO.getName());
        CouponDO existingCoupon = getOne(wrapper);
        if (existingCoupon != null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "该优惠券名称已存在");
        }

        CouponDO couponDO = new CouponDO();
        BeanUtils.copyProperties(couponDTO,couponDO);
        couponDO.setStatus(1); // 默认未使用

        return save(couponDO);
    }

    @Override
    public Page<CouponDO> listCoupons(CouponQuery query) {
        int pageNo = query.getPageNo() == null ? 1 : query.getPageNo();
        int pageSize = query.getPageSize() == null ? 10 : query.getPageSize();

        Page<CouponDO> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<CouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponDO::getDeleted,0)
                .like(query.getName()!= null && !query.getName().isEmpty(),CouponDO::getName,query.getName())
                .eq(query.getStatus() != null,CouponDO::getStatus, query.getStatus())
                .eq(query.getType() != null,CouponDO::getType, query.getType())
                .orderByDesc(CouponDO::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        CouponDO couponDO = getById(id);
        if (couponDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "优惠券不存在");
        }
        
        // 校验状态值是否合法 (1-未使用，2-已使用，3-已过期)
        if (status != 1 && status != 2 && status != 3) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "优惠券状态值不合法");
        }
        couponDO.setStatus(status);
        return updateById(couponDO);
    }

    @Override
    public Boolean deleteCoupon(Long id) {
        CouponDO couponDO = getById(id);
        if (couponDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "优惠券不存在");
        }

        return removeById(id);
    }
}