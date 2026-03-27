package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.AiConversation;
import org.apache.ibatis.annotations.Delete;

public interface AiConversationsMapper extends BaseMapper<AiConversation> {

    @Delete("DELETE FROM o_blog.ai_conversations WHERE session_id = #{sessionId}")
    void deleteBySessionId(String sessionId);

    @Delete("DELETE FROM o_blog.ai_conversations WHERE user_id = #{userId}")
    void deleteAllByUserId(Long userId);
}
