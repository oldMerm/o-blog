package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.AiMessages;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

public interface AiMessageMapper extends BaseMapper<AiMessages> {

    @Delete("DELETE FROM o_blog.ai_messages WHERE session_id = #{sessionId}")
    void deleteBySessionId(String sessionId);

    void deleteBySessionIds(List<String> sessionIds);
}
