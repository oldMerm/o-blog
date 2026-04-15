package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.response.AiResponse;
import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.AiMessagesDTO;
import io.github.oldmerman.model.po.AiConversation;
import io.github.oldmerman.model.po.AiMessages;
import io.github.oldmerman.model.rpcp.ChatRequest;
import io.github.oldmerman.model.vo.AiConversationVO;
import io.github.oldmerman.model.vo.AiMessagesVO;
import io.github.oldmerman.web.converter.AiConverter;
import io.github.oldmerman.web.mapper.AiConversationsMapper;
import io.github.oldmerman.web.mapper.AiMessageMapper;
import io.github.oldmerman.web.service.AiService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final AiMessageMapper messageMapper;

    private final AiConversationsMapper conversationsMapper;

    private final AiConverter converter;

    private final WebClient webClient;

    @Override
    public List<AiConversationVO> getSessions() {
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getUserId, UserContext.getUserId()).orderByDesc(AiConversation::getCreatedAt);
        return conversationsMapper.selectList(wrapper).stream().map(converter::poToVo).toList();
    }

    @Override
    public List<AiMessagesVO> getChatInfo(String sessionId) {
        LambdaQueryWrapper<AiMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessages::getSessionId, sessionId).orderByAsc(AiMessages::getCreatedAt);
        return messageMapper.selectList(wrapper).stream().map(converter::messagePoToVo).toList();
    }

    @Override
    public Mono<Result<Integer>> health() {
        return webClient.get()
                .uri("/agent/health")
                .retrieve()
                .bodyToMono(AiResponse.class)
                .map(response -> Result.success(Integer.parseInt(response.getData())))
                .onErrorResume(e -> Mono.just(Result.fail(BusErrorCode.AI_SYSTEM_ERROR)));
    }

    @Override
    public AiConversationVO createSession() {
        AiConversation session = new AiConversation();
        session.setUserId(UserContext.getUserId());
        session.setSessionDecr("新建会话");
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        conversationsMapper.insert(session);
        return converter.poToVo(session);
    }

    @Override
    public Mono<Result<AiMessagesVO>> chat(AiMessagesDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("[agent]用户：{}，请求会话。", userId);
        return webClient.post()
                .uri("/agent/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatRequest(dto.getSessionId(), userId.toString(), dto.getContent(), ""))
                .retrieve().bodyToMono(AiResponse.class)
                .flatMap(res -> {
                    AiMessagesVO vo = new AiMessagesVO();
                    vo.setSessionId(dto.getSessionId());
                    vo.setRole("ai");
                    vo.setContent(res.data);
                    return Mono.just(Result.success(vo));
                }).onErrorResume(e -> Mono.just(Result.fail(BusErrorCode.AI_SYSTEM_ERROR)));
    }

    @Override
    @Transactional
    public void deleteOneSession(String sessionId) {
        messageMapper.deleteBySessionId(sessionId);
        conversationsMapper.deleteBySessionId(sessionId);
    }

    @Override
    @Transactional
    public void deleteAllSession() {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<AiConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiConversation::getUserId, userId);
        List<String> sessionIds = conversationsMapper.selectList(queryWrapper).stream().map(AiConversation::getSessionId).toList();
        if (sessionIds.isEmpty()) return;
        messageMapper.deleteBySessionIds(sessionIds);
        conversationsMapper.deleteAllByUserId(userId);
    }


}
