package io.github.oldmerman.web.agent.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.agent.ReactAgent;
import io.github.oldmerman.agent.state.AgentState;
import io.github.oldmerman.core.api.chat.ChatClient;
import io.github.oldmerman.core.api.chat.messages.HumanMessage;
import io.github.oldmerman.core.api.chat.messages.Message;
import io.github.oldmerman.core.api.chat.messages.SystemMessage;
import io.github.oldmerman.core.tool.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    public void checkAgentMethod() throws JsonProcessingException {
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(tools);
        ReactAgent agent = ReactAgent.create(client, toolRegistry);
        Long start = System.currentTimeMillis();
        AgentState state = agent.invoke(List.of(
                new SystemMessage("你是老鱼人的助手，能够自己解决或通过工具解决问题"),
                new HumanMessage("随便挑一篇文章，给我讲讲里面的内容")));
        for (Message message : state.getMessages()) {
            System.out.println("消息类型：" + message.role() + " 消息：" + message.content());
        }
        Long end = System.currentTimeMillis();
        System.out.println("执行耗时: " + (end - start));
    }

}