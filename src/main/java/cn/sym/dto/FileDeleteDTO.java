package cn.sym.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除文件请求参数封装类
 *
 * @author user
 */
@Data
public class FileDeleteDTO {

    /**
     * 文件记录ID（必填）
     */
    @NotNull(message = "文件记录ID不能为空")
    private Long id;
}