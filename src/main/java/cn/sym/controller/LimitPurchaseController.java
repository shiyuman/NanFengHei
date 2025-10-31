package cn.sym.controller;

import cn.sym.dto.LimitPurchaseAddDTO;
import cn.sym.dto.LimitPurchaseEditDTO;
import cn.sym.dto.LimitPurchaseQueryDTO;
import cn.sym.entity.LimitPurchaseDO;
import cn.sym.response.RestResult;
import cn.sym.service.LimitPurchaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 限购配置控制器
 *
 * @author user
 */
@Slf4j
@Api("限购配置管理")
@RestController
@RequestMapping("/limit-purchase")
public class LimitPurchaseController {

    @Autowired
    private LimitPurchaseService limitPurchaseService;

    /**
     * 新增限购配置
     *
     * @param addDTO 新增参数对象
     * @return RestResult 结果
     */
    @PostMapping("/add")
    @ApiOperation("新增限购配置")
    public RestResult<Boolean> addLimitPurchase(@RequestBody @Valid LimitPurchaseAddDTO addDTO) {
        return limitPurchaseService.addLimitPurchase(addDTO);
    }

    /**
     * 编辑限购配置
     *
     * @param editDTO 编辑参数对象
     * @return RestResult 结果
     */
    @PutMapping("/edit")
    @ApiOperation("编辑限购配置")
    public RestResult<Boolean> editLimitPurchase(@RequestBody @Valid LimitPurchaseEditDTO editDTO) {
        return limitPurchaseService.editLimitPurchase(editDTO);
    }

    /**
     * 查询限购配置详情
     *
     * @param queryDTO 查询参数对象
     * @return RestResult 结果
     */
    @GetMapping("/detail")
    @ApiOperation("查询限购配置详情")
    public RestResult<LimitPurchaseDO> getLimitPurchaseDetail(@Valid LimitPurchaseQueryDTO queryDTO) {
        return limitPurchaseService.getLimitPurchaseDetail(queryDTO);
    }
}