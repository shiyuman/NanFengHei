package cn.sym.controller;

import cn.sym.dto.PreSaleTicketAddDTO;
import cn.sym.dto.PreSaleTicketEditDTO;
import cn.sym.dto.PreSaleTicketQueryDTO;
import cn.sym.entity.PreSaleTicketDO;
import cn.sym.response.RestResult;
import cn.sym.service.PreSaleTicketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 早鸟票预售配置控制器
 *
 * @author user
 */
@Slf4j
@Api("早鸟票预售配置管理")
@RestController
@RequestMapping("/pre-sale-ticket")
public class PreSaleTicketController {

    @Autowired
    private PreSaleTicketService preSaleTicketService;

    /**
     * 新增早鸟票预售配置
     *
     * @param addDTO 新增参数对象
     * @return RestResult 结果
     */
    @PostMapping("/add")
    @ApiOperation("新增早鸟票预售配置")
    public RestResult<Boolean> addPreSaleTicket(@RequestBody @Valid PreSaleTicketAddDTO addDTO) {
        return preSaleTicketService.addPreSaleTicket(addDTO);
    }

    /**
     * 编辑早鸟票预售配置
     *
     * @param editDTO 编辑参数对象
     * @return RestResult 结果
     */
    @PutMapping("/edit")
    @ApiOperation("编辑早鸟票预售配置")
    public RestResult<Boolean> editPreSaleTicket(@RequestBody @Valid PreSaleTicketEditDTO editDTO) {
        return preSaleTicketService.editPreSaleTicket(editDTO);
    }

    /**
     * 查询早鸟票预售配置详情
     *
     * @param queryDTO 查询参数对象
     * @return RestResult 结果
     */
    @GetMapping("/detail")
    @ApiOperation("查询早鸟票预售配置详情")
    public RestResult<PreSaleTicketDO> getPreSaleTicketDetail(@Valid PreSaleTicketQueryDTO queryDTO) {
        return preSaleTicketService.getPreSaleTicketDetail(queryDTO);
    }
}