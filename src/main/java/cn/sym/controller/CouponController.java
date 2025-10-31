package cn.sym.controller;

import cn.sym.dto.CouponDTO;
import cn.sym.dto.CouponQuery;
import cn.sym.entity.CouponDO;
import cn.sym.common.response.RestResult;
import cn.sym.common.response.ResultCodeConstant;
import cn.sym.service.CouponService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *   优惠券控制器
 * </p>
 * @author user
 */
@Api("优惠券管理")
@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * 新增优惠券
     *
     * @param couponDTO 优惠券信息
     * @return 结果
     */
    @PostMapping("/add")
    @ApiOperation("新增优惠券")
    public RestResult<Boolean> addCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        Boolean result = couponService.addCoupon(couponDTO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 查询优惠券列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("/list")
    @ApiOperation("查询优惠券列表")
    public RestResult<Page<CouponDO>> listCoupons(@Valid CouponQuery query) {
        Page<CouponDO> result = couponService.listCoupons(query);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 更新优惠券状态
     *
     * @param id 优惠券ID
     * @param status 状态值
     * @return 结果
     */
    @PutMapping("/status/{id}")
    @ApiOperation("更新优惠券状态")
    public RestResult<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Boolean result = couponService.updateStatus(id, status);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 删除优惠券
     *
     * @param id 优惠券ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除优惠券")
    public RestResult<Boolean> deleteCoupon(@PathVariable Long id) {
        Boolean result = couponService.deleteCoupon(id);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }
}