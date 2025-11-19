package cn.sym.service.impl;

import cn.sym.dto.FileDeleteDTO;
import cn.sym.dto.FileQueryDTO;
import cn.sym.dto.FileUploadDTO;
import cn.sym.entity.FileDO;
import cn.sym.repository.FileMapper;
import cn.sym.common.response.RestResult;
import cn.sym.common.response.ResultCodeConstant;
import cn.sym.service.FileService;
import cn.sym.utils.OssUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Date;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文件管理服务实现类
 *
 * @author user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper,FileDO>  implements FileService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 最大文件大小为50MB

    private final FileMapper fileMapper;
    private final OssUtil ossUtil;

    @Override
    public RestResult<Boolean> uploadFile(FileUploadDTO dto) {
        try {
            // 校验文件是否为空
            if (dto.getFile() == null || dto.getFile().length == 0) {
                return new RestResult<>(ResultCodeConstant.CODE_000001, "文件不能为空");
            }

            // 校验文件大小是否超过限制
            if (dto.getFile().length > MAX_FILE_SIZE) {
                return new RestResult<>(ResultCodeConstant.CODE_000001, "文件大小超出限制");
            }

            // 调用OSS上传文件并获取存储路径Key
            String ossKey = ossUtil.upload(dto.getFile(), dto.getFileName(), dto.getMimeType());
            if (ossKey == null) {
                return new RestResult<>(ResultCodeConstant.CODE_999999, "文件上传失败");
            }

            // 将文件信息保存至file_storage表中
            FileDO fileDO = new FileDO();
            fileDO.setOriginalName(dto.getFileName());
            fileDO.setOssKey(ossKey);
            fileDO.setFileSize((long) dto.getFile().length);
            fileDO.setMimeType(dto.getMimeType());
            fileDO.setUploadTime(new Date());
            fileDO.setCreateTime(new Date());
            fileDO.setUpdateTime(new Date());

            boolean success = fileMapper.insert(fileDO) > 0;
            if (!success) {
                return new RestResult<>(ResultCodeConstant.CODE_999999, "文件信息保存失败");
            }

            return new RestResult<>(ResultCodeConstant.CODE_000000, "上传成功", true);
        } catch (Exception e) {
            log.error("文件上传异常: {}", e.getMessage(), e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, "服务器内部错误");
        }
    }

    @Override
    public RestResult<Boolean> deleteFile(FileDeleteDTO dto) {
        try {
            // 根据ID查询文件记录是否存在
            LambdaQueryWrapper<FileDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileDO::getId, dto.getId());
            FileDO fileDO = fileMapper.selectOne(wrapper);
            if (fileDO == null) {
                return new RestResult<>(ResultCodeConstant.CODE_000001, "文件记录不存在");
            }

            // 从OSS中删除该文件
            boolean ossDeleted = ossUtil.delete(fileDO.getOssKey());
            if (!ossDeleted) {
                return new RestResult<>(ResultCodeConstant.CODE_999999, "文件删除失败");
            }

            // 从数据库中删除该文件记录
            int deletedRows = fileMapper.deleteById(dto.getId());
            if (deletedRows <= 0) {
                return new RestResult<>(ResultCodeConstant.CODE_999999, "数据库删除失败");
            }

            return new RestResult<>(ResultCodeConstant.CODE_000000, "删除成功", true);
        } catch (Exception e) {
            log.error("文件删除异常: {}", e.getMessage(), e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, "服务器内部错误");
        }
    }

    @Override
    public RestResult<FileDO> queryFileInfo(FileQueryDTO dto) {
        try {
            // 根据ID查询文件记录是否存在
            LambdaQueryWrapper<FileDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileDO::getId, dto.getId());
            FileDO fileDO = fileMapper.selectOne(wrapper);
            if (fileDO == null) {
                return new RestResult<>(ResultCodeConstant.CODE_000001, "文件记录不存在");
            }

            return new RestResult<>(ResultCodeConstant.CODE_000000, "查询成功", fileDO);
        } catch (Exception e) {
            log.error("查询文件详情异常: {}", e.getMessage(), e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, "服务器内部错误");
        }
    }
}