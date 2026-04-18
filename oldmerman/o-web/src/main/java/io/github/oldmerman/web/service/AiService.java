package io.github.oldmerman.web.service;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.AiMessagesDTO;
import io.github.oldmerman.model.vo.AiConversationVO;
import io.github.oldmerman.model.vo.AiMessagesVO;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * ai功能相关业务
 * @author oldmerman
 * @date 2026-3-23
 */
public interface AiService {

    /**
     * 用户获取会话信息
     *
     * @return 会话信息对应的集合
     */
    List<AiConversationVO> getSessions();

    /**
     * 获取对话信息
     *
     * @param sessionId 唯一会话Id
     * @return 对话信息
     */
    List<AiMessagesVO> getChatInfo(String sessionId);

    /**
     * SSE流式获取agent服务响应
     *
     * @param sessionId 会话id
     * @param content 提示内容
     * @return Json块
     */
    Flux<ServerSentEvent<String>> stream(String sessionId, String content, String token);

    /**
     * ai服务心跳
     *
     * @return 1 -> 正常
     */
    Mono<Result<Integer>> health();

    /**
     * 用户创建会话
     */
    AiConversationVO createSession();

    /**
     * ai基础会话
     *
     * @param dto 会话id和请求内容
     * @return 会话信息
     */
    Mono<Result<AiMessagesVO>> chat(AiMessagesDTO dto);

    /**
     * 删除单个会话
     */
    void deleteOneSession(String sessionId);

    /**
     * 删除所有会话
     */
    void deleteAllSession();
}
