package cn.sym.service;

import cn.sym.dto.FileDeleteDTO;
import cn.sym.dto.FileQueryDTO;
import cn.sym.dto.FileUploadDTO;
import cn.sym.entity.FileDO;
import cn.sym.common.response.RestResult;

/**
 * 文件管理服务接口
 *
 * @author user
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param dto 文件上传参数
     * @return 上传结果
     */
    RestResult<Boolean> uploadFile(FileUploadDTO dto);

    /**
     * 删除文件
     *
     * @param dto 删除文件参数
     * @return 删除结果
     */
    RestResult<Boolean> deleteFile(FileDeleteDTO dto);

    /**
     * 查询文件详情
     *
     * @param dto 查询文件参数
     * @return 文件详情
     */
    RestResult<FileDO> queryFileInfo(FileQueryDTO dto);
}