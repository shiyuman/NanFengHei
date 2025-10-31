package cn.sym.controller;

import cn.sym.common.constant.ResultCodeConstant;
import cn.sym.common.exception.BusinessException;
import cn.sym.common.response.RestResult;
import cn.sym.dto.CacheQuery;
import cn.sym.dto.CacheUpdate;
import cn.sym.service.CacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("cache")
@Api("缓存管理")
public class CacheController {

    @Autowired
    private CacheService cacheService;

    /**
     * 缓存数据查询
     *
     * @param cacheQuery 缓存查询条件
     * @return
     */
    @GetMapping("/query")
    @ApiOperation("缓存数据查询")
    public RestResult<String> queryCacheData(@Validated @ModelAttribute CacheQuery cacheQuery) {
        try {
            String cacheData = cacheService.getCacheData(cacheQuery.getCacheKey());
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, cacheData);
        } catch (BusinessException e) {
            return new RestResult<>(e.getCode(), e.getMsg());
        }
    }

    /**
     * 缓存数据更新
     *
     * @param cacheUpdate 缓存更新条件
     * @return
     */
    @PostMapping("/update")
    @ApiOperation("缓存数据更新")
    public RestResult<Void> updateCacheData(@Validated @RequestBody CacheUpdate cacheUpdate) {
        try {
            cacheService.updateCacheData(cacheUpdate.getCacheKey(), cacheUpdate.getCacheValue());
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG);
        } catch (BusinessException e) {
            return new RestResult<>(e.getCode(), e.getMsg());
        }
    }

    /**
     * 缓存数据删除
     *
     * @param cacheQuery 缓存查询条件
     * @return
     */
    @DeleteMapping("/delete")
    @ApiOperation("缓存数据删除")
    public RestResult<Void> deleteCacheData(@Validated @ModelAttribute CacheQuery cacheQuery) {
        try {
            cacheService.deleteCacheData(cacheQuery.getCacheKey());
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG);
        } catch (BusinessException e) {
            return new RestResult<>(e.getCode(), e.getMsg());
        }
    }
}