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

    @TableField("from_user_id")
    private Long fromUserId;

    @TableField("to_user_id")
    private Long toUserId;

    @TableField("content")
    private String content;

    @TableField("create_time")
    private Date createTime;
}