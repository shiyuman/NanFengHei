package cn.sym.controller;

import cn.sym.dto.*;
import cn.sym.dto.FileDeleteDTO;
import cn.sym.dto.FileQueryDTO;
import cn.sym.dto.FileUploadDTO;
import cn.sym.entity.FileDO;
import cn.sym.response.RestResult;
import cn.sym.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文件管理控制器
 *
 * @author user
 */
@Slf4j
@Api("文件管理")
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     *
     * @param dto 文件上传参数
     * @return 上传结果
     */
    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public RestResult<Boolean> uploadFile(@Valid @RequestBody FileUploadDTO dto) {
        return fileService.uploadFile(dto);
    }

    /**
     * 删除文件
     *
     * @param dto 删除文件参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    @ApiOperation("删除文件")
    public RestResult<Boolean> deleteFile(@Valid @RequestBody FileDeleteDTO dto) {
        return fileService.deleteFile(dto);
    }

    /**
     * 查询文件详情
     *
     * @param dto 查询文件参数
     * @return 文件详情
     */
    @GetMapping("/info")
    @ApiOperation("查询文件详情")
    public RestResult<FileDO> fileInfo(@Valid FileQueryDTO dto) {
        return fileService.queryFileInfo(dto);
    }
}