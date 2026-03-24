package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.AiMessagesDTO;
import io.github.oldmerman.model.po.AiConversation;
import io.github.oldmerman.model.vo.AiConversationVO;
import io.github.oldmerman.model.vo.AiMessagesVO;
import io.github.oldmerman.web.converter.AiConverter;
import io.github.oldmerman.web.mapper.AiConversationsMapper;
import io.github.oldmerman.web.mapper.AiMessageMapper;
import io.github.oldmerman.web.service.AiService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
        wrapper.eq(AiConversation::getUserId, UserContext.getUserId())
                .orderByDesc(AiConversation::getCreatedAt);
        return conversationsMapper.selectList(wrapper).stream().map(converter::poToVo).toList();
    }

    @Override
    public void createSession() {
        AiConversation session = new AiConversation();
        session.setUserId(UserContext.getUserId());
        session.setSessionDecr("新建会话");
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        conversationsMapper.insert(session);
    }

    @Override
    public Mono<Result<AiMessagesVO>> chat(AiMessagesDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("[agent]用户：{}，请求会话。", userId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/agent/chat")
                        .queryParam("session_id", dto.getSessionId())
                        .queryParam("content", dto.getContent())
                        .queryParam("user_id", userId)
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .map(str -> {
                    AiMessagesVO vo = new AiMessagesVO();
                    vo.setRole("ai");
                    vo.setContent(str);
                    return Result.success(vo);
                })
                .onErrorResume(e -> {
                    throw new BusinessException(BusErrorCode.AI_SYSTEM_ERROR);
                });
    }


}
