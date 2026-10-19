package io.github.oldmerman.web.agent.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.oldmerman.agent.ReactAgent;
import io.github.oldmerman.agent.state.AgentState;
import io.github.oldmerman.core.DeepseekClient;
import io.github.oldmerman.core.ToolRegistry;
import io.github.oldmerman.core.entity.messages.HumanMessage;
import io.github.oldmerman.core.entity.messages.Message;
import io.github.oldmerman.core.entity.messages.SystemMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ArticleToolsTest {

    @Autowired
    private ArticleTools tools;

    @Autowired
    private DeepseekClient client;

    @Test
    public void checkAgentMethod() throws JsonProcessingException {
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(tools);
        ReactAgent agent = ReactAgent.create(client, toolRegistry);
        AgentState state = agent.invoke(List.of(
                new SystemMessage("你是老鱼人的助手，能够自己解决或通过工具解决问题"),
                new HumanMessage("文章id：2047884559617757184，讲了什么内容？")));
        for (Message message : state.getMessages()) {
            System.out.println("消息类型：" + message.role() + " 消息：" + message.content());
        }
    }

}