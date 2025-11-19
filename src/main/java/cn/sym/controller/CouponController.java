package cn.sym.controller;

import cn.sym.dto.CouponDTO;
import cn.sym.dto.CouponQuery;
import cn.sym.entity.CouponDO;
import cn.sym.common.response.RestResult;
import cn.sym.common.response.ResultCodeConstant;
import cn.sym.service.CouponService;
import cn.sym.utils.RateLimiterUtil;
import cn.sym.common.exception.BusinessException;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * <p>
 *   优惠券控制器
 * </p>
 * @author user
 */
@Api("优惠券管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;
    private final RateLimiterUtil rateLimiterUtil;

    /**
     * 新增优惠券
     *
     * @param couponDTO 优惠券信息
     * @return 结果
     */
    @PostMapping("/add")
    @ApiOperation("新增优惠券")
    public RestResult<Boolean> addCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        // 对新增优惠券接口进行限流，每个IP每分钟最多10次操作
        String key = "coupon:add:" + getClientIP();
        if (!rateLimiterUtil.isAllowed(key, 10, 1)) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "操作过于频繁，请稍后再试");
        }
        
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
        // 对查询接口进行限流，每个IP每秒最多5次操作
        String key = "coupon:list:" + getClientIP();
        if (!rateLimiterUtil.isAllowed(key, 20, 5)) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "操作过于频繁，请稍后再试");
        }
        
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
        // 对状态更新接口进行限流，每个IP每分钟最多30次操作
        String key = "coupon:update:" + getClientIP();
        if (!rateLimiterUtil.isAllowed(key, 30, 1)) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "操作过于频繁，请稍后再试");
        }
        
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
        // 对删除接口进行限流，每个IP每分钟最多5次操作
        String key = "coupon:delete:" + getClientIP();
        if (!rateLimiterUtil.isAllowed(key, 5, 1)) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "操作过于频繁，请稍后再试");
        }
        
        Boolean result = couponService.deleteCoupon(id);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIP() {
        HttpServletRequest request =
            ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 多个IP地址时，取第一个非unknown的IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}