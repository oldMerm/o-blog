package io.github.oldmerman.web.agent.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.agent.ReactAgent;
import io.github.oldmerman.agent.state.AgentState;
import io.github.oldmerman.agent.utils.TokenTracker;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.core.api.chat.ChatClient;
import io.github.oldmerman.core.api.chat.handle.ChunkListener;
import io.github.oldmerman.core.api.chat.messages.HumanMessage;
import io.github.oldmerman.core.api.chat.messages.SystemMessage;
import io.github.oldmerman.core.tool.ToolRegistry;
import io.github.oldmerman.model.po.AgentChat;
import io.github.oldmerman.web.mapper.AgentChatMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ArticleToolsTest {

    @Autowired
    private AgentChatMapper agentChatMapper;
    @Autowired
    private ArticleTools tools;
    @Autowired
    private ChatClient client;
    @Autowired
    @Qualifier("ignoreNullMapper")
    private ObjectMapper mapper;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void checkAgentMethod() throws JsonProcessingException, InterruptedException {
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(tools);
        ReactAgent agent = ReactAgent.create(client, toolRegistry, false);
        Long start = System.currentTimeMillis();
        AgentState state = AgentState.create(List.of(
                new SystemMessage("你是老鱼人的助手，能够自己解决或通过工具解决问题"),
                new HumanMessage("你好啊？！"))
        );
        state.setTracker(new TokenTracker());
        agent.invoke(state);
//        for (Message message : state.getMessages()) {
//            System.out.println("消息类型：" + message.role() + " 消息：" + message.content());
//        }
        String jsonStr = mapper.writeValueAsString(state);
        System.out.println(jsonStr);
        AgentState dejson = mapper.readValue(jsonStr, AgentState.class);
        System.out.println(dejson);
        Long end = System.currentTimeMillis();
        System.out.println(dejson.getTracker().getUsage("deepseek-flash"));
        System.out.println("执行耗时: " + (end - start));
    }

    @Test
    public void checkReqBody() throws JsonProcessingException {
        AgentChat po = agentChatMapper.selectByThreadId("80fb870a-66d8-4436-b04b-c234def94eb9");
        Long id = po.getId();
        AgentState state = objectMapper.readValue(po.getState(), AgentState.class);
        System.out.println(objectMapper.writeValueAsString(state));

        AgentState state1 = mapper.readValue(po.getState(), AgentState.class);
        System.out.println(mapper.writeValueAsString(state1));

    }

}