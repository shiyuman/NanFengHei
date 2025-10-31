package cn.sym.service;

import cn.sym.dto.CouponDTO;
import cn.sym.dto.CouponQuery;
import cn.sym.entity.CouponDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *   优惠券服务接口
 * </p>
 * @author user
 */
public interface CouponService extends IService<CouponDO> {

    /**
     * 新增优惠券
     * @param couponDTO 优惠券信息
     * @return 是否新增成功
     */
    Boolean addCoupon(CouponDTO couponDTO);

    /**
     * 分页查询优惠券列表
     * @param query 查询参数
     * @return 分页结果
     */
    Page<CouponDO> listCoupons(CouponQuery query);

    /**
     * 更新优惠券状态
     * @param id 优惠券ID
     * @param status 状态值
     * @return 是否更新成功
     */
    Boolean updateStatus(Long id, Integer status);

    /**
     * 删除优惠券
     * @param id 优惠券ID
     * @return 是否删除成功
     */
    Boolean deleteCoupon(Long id);
}