package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件导入参数
 *
 * @author user
 */
@Data
@ApiModel("文件导入参数")
public class FileImportDTO {

    /**
     * 上传的Excel文件
     */
    @ApiModelProperty(value = "上传的Excel文件", required = true)
    private MultipartFile file;
}