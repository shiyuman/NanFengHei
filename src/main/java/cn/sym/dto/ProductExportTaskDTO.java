package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("商品导出任务信息")
public class ProductExportTaskDTO {

    @ApiModelProperty(value = "任务ID")
    private String taskId;

    @ApiModelProperty(value = "任务状态：0-处理中，1-已完成，2-失败")
    private Integer status;

    @ApiModelProperty(value = "文件下载地址")
    private String downloadUrl;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;
}

