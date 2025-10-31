package cn.sym.service.impl;

import cn.sym.dto.CouponDTO;
import cn.sym.dto.CouponQuery;
import cn.sym.entity.CouponDO;
import cn.sym.exception.BusinessException;
import cn.sym.repository.CouponMapper;
import cn.sym.response.ResultCodeConstant;
import cn.sym.service.CouponService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
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

        // 检查是否存在相同名称的优惠券
        QueryWrapper<CouponDO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", couponDTO.getName());
        CouponDO existingCoupon = this.getOne(wrapper);
        if (existingCoupon != null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "该优惠券名称已存在");
        }

        // 构建优惠券DO对象并保存
        CouponDO couponDO = new CouponDO();
        couponDO.setName(couponDTO.getName());
        couponDO.setType(couponDTO.getType());
        couponDO.setDiscountAmount(couponDTO.getDiscountAmount());
        couponDO.setMinAmount(couponDTO.getMinAmount());
        couponDO.setStartTime(couponDTO.getStartTime());
        couponDO.setEndTime(couponDTO.getEndTime());
        couponDO.setStatus(1); // 默认未使用
        couponDO.setCreateBy("admin"); // 示例创建者
        couponDO.setCreateTime(new Date());
        couponDO.setUpdateBy("admin"); // 示例修改者
        couponDO.setUpdateTime(new Date());

        return this.save(couponDO);
    }

    @Override
    public Page<CouponDO> listCoupons(CouponQuery query) {
        int pageNo = query.getPageNo() == null ? 1 : query.getPageNo();
        int pageSize = query.getPageSize() == null ? 10 : query.getPageSize();

        Page<CouponDO> page = new Page<>(pageNo, pageSize);
        return this.page(page);
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        CouponDO couponDO = this.getById(id);
        if (couponDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "优惠券不存在");
        }
        couponDO.setStatus(status);
        couponDO.setUpdateTime(new Date());
        return this.updateById(couponDO);
    }

    @Override
    public Boolean deleteCoupon(Long id) {
        CouponDO couponDO = this.getById(id);
        if (couponDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "优惠券不存在");
        }
        return this.removeById(id);
    }
}