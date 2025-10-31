package cn.sym.repository;

import cn.sym.entity.ChatMessage;
import java.util.List;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageMapper {
    @Insert("INSERT INTO chat_message (from_user_id, to_user_id, content, create_time) VALUES (#{fromUserId}, #{toUserId}, #{content}, #{createTime})")
    int insert(ChatMessage chatMessage);

    @Select("SELECT * FROM chat_message WHERE from_user_id = #{userId} OR to_user_id = #{userId}")
    List<ChatMessage> findByUserId(Long userId);
}