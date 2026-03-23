package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.oldmerman.model.po.AiConversation;
import io.github.oldmerman.web.mapper.AiConversationsMapper;
import io.github.oldmerman.web.mapper.AiMessageMapper;
import io.github.oldmerman.web.service.AiService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiMessageMapper messageMapper;

    private final AiConversationsMapper conversationsMapper;

    @Override
    public List<AiConversation> getSessions() {
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getUserId, UserContext.getUserId())
                .orderByDesc(AiConversation::getCreatedAt);
        return conversationsMapper.selectList(wrapper);
    }

    @Override
    public void createSession() {
        AiConversation session = new AiConversation();
        session.setUserId(UserContext.getUserId());
        session.setSessionDecr("新建会话");
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        conversationsMapper.insert(session);
    }


}
