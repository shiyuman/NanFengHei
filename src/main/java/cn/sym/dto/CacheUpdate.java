package cn.sym.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CacheUpdate {

    @NotBlank(message = "缓存key不能为空")
    @ApiModelProperty(value = "缓存key")
    private String cacheKey;

    @NotBlank(message = "缓存value不能为空")
    @ApiModelProperty(value = "缓存value")
    private String cacheValue;
}