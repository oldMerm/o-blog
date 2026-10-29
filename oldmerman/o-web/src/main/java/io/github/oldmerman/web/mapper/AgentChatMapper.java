package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.AgentChat;
import org.apache.ibatis.annotations.Select;

public interface AgentChatMapper extends BaseMapper<AgentChat> {

    @Select("SELECT * FROM o_blog.o_agent_chat WHERE thread_id = #{threadId}")
    AgentChat selectByThreadId(String threadId);
}
