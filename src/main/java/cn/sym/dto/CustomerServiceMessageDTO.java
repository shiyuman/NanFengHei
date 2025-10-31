package cn.sym.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerServiceMessageDTO {
    @NotNull(message = "发送方用户ID不能为空")
    private Long fromUserId;

    @NotNull(message = "接收方用户ID不能为空")
    private Long toUserId;

    @NotNull(message = "消息内容不能为空")
    private String content;
}