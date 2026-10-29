package io.github.oldmerman.web.agent.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.agent.ReactAgent;
import io.github.oldmerman.agent.state.AgentState;
import io.github.oldmerman.agent.utils.TokenTracker;
import io.github.oldmerman.core.api.chat.ChatClient;
import io.github.oldmerman.core.api.chat.messages.HumanMessage;
import io.github.oldmerman.core.api.chat.messages.SystemMessage;
import io.github.oldmerman.core.tool.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ArticleToolsTest {

    @Autowired
    private ArticleTools tools;

    @Autowired
    private ChatClient client;

    @Autowired
    @Qualifier("ignoreNullMapper")
    private ObjectMapper mapper;

    @Test
    public void checkAgentMethod() throws JsonProcessingException, InterruptedException {
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(tools);
        ReactAgent agent = ReactAgent.create(client, toolRegistry, false);
        Long start = System.currentTimeMillis();
        AgentState state = AgentState.create(List.of(
                new SystemMessage("你是老鱼人的助手，能够自己解决或通过工具解决问题"),
                new HumanMessage("老鱼人的博客里有哪些文章？"))
        );
        state.setTracker(new TokenTracker());
        agent.invoke(state);
//        for (Message message : state.getMessages()) {
//            System.out.println("消息类型：" + message.role() + " 消息：" + message.content());
//        }
        String jsonStr = mapper.writeValueAsString(state);
        System.out.println(jsonStr);
        System.out.println(mapper.readValue(jsonStr, AgentState.class));
        Long end = System.currentTimeMillis();
        System.out.println("执行耗时: " + (end - start));
    }

}