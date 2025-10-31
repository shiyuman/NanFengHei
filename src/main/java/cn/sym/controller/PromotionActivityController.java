package cn.sym.controller;

import cn.sym.dto.PromotionActivityDTO;
import cn.sym.dto.PromotionActivityQuery;
import cn.sym.entity.PromotionActivityDO;
import cn.sym.response.RestResult;
import cn.sym.service.PromotionActivityService;
import cn.sym.utils.ResultCodeConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *   促销活动控制器
 * </p>
 * @author user
 */
@Slf4j
@Api("促销活动管理")
@RestController
@RequestMapping("/promotion")
public class PromotionActivityController {

    @Autowired
    private PromotionActivityService promotionActivityService;

    /**
     * 新增促销活动
     *
     * @param promotionActivityDTO 活动信息
     * @return 结果
     */
    @PostMapping("/add")
    @ApiOperation("新增促销活动")
    public RestResult<Boolean> addPromotionActivity(@RequestBody @Valid PromotionActivityDTO promotionActivityDTO) {
        boolean result = promotionActivityService.addPromotionActivity(promotionActivityDTO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 更新促销活动
     *
     * @param promotionActivityDTO 活动信息
     * @return 结果
     */
    @PutMapping("/update")
    @ApiOperation("更新促销活动")
    public RestResult<Boolean> updatePromotionActivity(@RequestBody @Valid PromotionActivityDTO promotionActivityDTO) {
        boolean result = promotionActivityService.updatePromotionActivity(promotionActivityDTO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 删除促销活动
     *
     * @param id 活动ID
     * @return 结果
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除促销活动")
    public RestResult<Boolean> deletePromotionActivity(@PathVariable Long id) {
        boolean result = promotionActivityService.deletePromotionActivity(id);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 查询促销活动列表
     *
     * @param query 查询条件
     * @return 结果
     */
    @GetMapping("/list")
    @ApiOperation("查询促销活动列表")
    public RestResult<com.baomidou.mybatisplus.extension.plugins.pagination.Page<PromotionActivityDO>> listPromotionActivities(PromotionActivityQuery query) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<PromotionActivityDO> result = promotionActivityService.listPromotionActivities(query);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 获取促销活动详情
     *
     * @param id 活动ID
     * @return 结果
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("获取促销活动详情")
    public RestResult<PromotionActivityDO> getPromotionActivityDetail(@PathVariable Long id) {
        PromotionActivityDO result = promotionActivityService.getPromotionActivityDetail(id);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }
}