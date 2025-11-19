package cn.sym.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.util.Date;

@Data
public class ProductExportTask {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务状态：0-处理中，1-已完成，2-失败
     */
    private Integer status;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date createTime;

    /**
     * 完成时间
     */
    private Date finishTime;
}

