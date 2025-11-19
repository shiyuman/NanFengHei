package cn.sym.entity;

import java.util.Date;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_message")
public class ChatMessage {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private Date createTime;
}