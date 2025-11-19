package cn.sym.controller;

import cn.sym.dto.LimitPurchaseAddDTO;
import cn.sym.dto.LimitPurchaseEditDTO;
import cn.sym.dto.LimitPurchaseQueryDTO;
import cn.sym.entity.LimitPurchaseDO;
import cn.sym.common.response.RestResult;
import cn.sym.service.LimitPurchaseService;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 限购配置控制器
 *
 * @author user
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/limit-purchase")
public class LimitPurchaseController {

    private final LimitPurchaseService limitPurchaseService;

    /**
     * 新增限购配置
     *
     * @param addDTO 新增参数对象
     * @return RestResult 结果
     */
    @PostMapping("/add")
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
    public RestResult<LimitPurchaseDO> getLimitPurchaseDetail(@Valid LimitPurchaseQueryDTO queryDTO) {
        return limitPurchaseService.getLimitPurchaseDetail(queryDTO);
    }
}