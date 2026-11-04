package io.github.oldmerman.web.agent.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.core.tool.Tool;
import io.github.oldmerman.core.tool.ToolContext;
import io.github.oldmerman.model.po.User;
import io.github.oldmerman.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserTools {

    private final UserMapper userMapper;

    @Qualifier("ignoreNullMapper")
    private final ObjectMapper objectMapper;

    @Tool(name = "get_user_info", description = "获取用户个人信息(包括用户名，账号创建时长，文章数量)")
    public String getUserInfo(ToolContext ctx) {
        Long userId = ctx.get("userId", Long.class);
        if (userId == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        log.info("[模型工具调用]get_user_info，获取用户：{} 相关信息", user.getUsername());
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", user.getUsername());
        node.put("createDuration", ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now()));
        node.put("articleNumber", user.getArticle());
        return node.toString();
    }

}
