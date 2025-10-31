package cn.sym.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文件上传请求参数封装类
 *
 * @author user
 */
@Data
public class FileUploadDTO {

    /**
     * 文件流（必填）
     */
    @NotNull(message = "文件不能为空")
    private byte[] file;

    /**
     * 原始文件名（必填）
     */
    @NotBlank(message = "原始文件名不能为空")
    private String fileName;

    /**
     * MIME类型（必填）
     */
    @NotBlank(message = "MIME类型不能为空")
    private String mimeType;
}