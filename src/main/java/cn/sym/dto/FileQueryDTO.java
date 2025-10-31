package cn.sym.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询文件详情请求参数封装类
 *
 * @author user
 */
@Data
public class FileQueryDTO {

    /**
     * 文件记录ID（必填）
     */
    @NotNull(message = "文件记录ID不能为空")
    private Long id;
}