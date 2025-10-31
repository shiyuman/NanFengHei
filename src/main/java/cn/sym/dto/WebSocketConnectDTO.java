package cn.sym.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WebSocketConnectDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "会话ID不能为空")
    private String sessionId;
}