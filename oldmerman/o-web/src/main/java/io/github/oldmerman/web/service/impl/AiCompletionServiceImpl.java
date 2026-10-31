package io.github.oldmerman.web.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.agent.ReactAgent;
import io.github.oldmerman.agent.state.AgentState;
import io.github.oldmerman.agent.utils.TokenTracker;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.util.IdGenerator;
import io.github.oldmerman.core.tool.ToolContext;
import io.github.oldmerman.model.po.AgentChat;
import io.github.oldmerman.web.agent.prompt.BlogSystemPrompt;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.core.api.chat.ChatClient;
import io.github.oldmerman.core.api.chat.handle.ChunkListener;
import io.github.oldmerman.core.api.chat.handle.StreamHandle;
import io.github.oldmerman.core.api.chat.messages.HumanMessage;
import io.github.oldmerman.core.api.chat.messages.SystemMessage;
import io.github.oldmerman.core.api.chat.model.ToolCall;
import io.github.oldmerman.core.api.chat.param.CompletionRequest;
import io.github.oldmerman.core.enums.FinishReason;
import io.github.oldmerman.core.enums.ToolChoice;
import io.github.oldmerman.core.tool.ToolRegistry;
import io.github.oldmerman.model.dto.AgentChatDTO;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.web.agent.tool.ArticleTools;
import io.github.oldmerman.web.mapper.AgentChatMapper;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.service.AiCompletionService;
import io.github.oldmerman.web.service.OssService;
import io.github.oldmerman.web.util.SseChunkListener;
import io.github.oldmerman.web.util.UserContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static io.github.oldmerman.web.agent.prompt.ArticleDigestPrompt.ARTICLE_DIGEST_PROMPT;
import static io.github.oldmerman.web.agent.prompt.ArticleDigestPrompt.TECH_RULES;
import static io.github.oldmerman.web.agent.prompt.ArticleDigestPrompt.LIFE_RULES;
import static io.github.oldmerman.web.agent.prompt.BlogSystemPrompt.BLOG_CHAT_PROMPT;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiCompletionServiceImpl implements AiCompletionService {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private final ChatClient client;

    private final OssService ossService;

    private final AgentChatMapper agentChatMapper;

    private final ArticleMapper articleMapper;

    private final StringRedisTemplate redisTemplate;

    @Qualifier("ignoreNullMapper")
    private final ObjectMapper objectMapper;

    /**
     * article tools
     */
    private final ArticleTools tools;

    private ReactAgent webChatAgent;

    @PostConstruct
    public void agentInitialization() {
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(tools);

        webChatAgent = ReactAgent.create(client, toolRegistry, false);
    }

    @Override
    public SseEmitter generateArticleDigest(Long articleId) {
        String digestCache = redisTemplate.opsForValue().get(RedisPrefix.ARTICLE_DIGEST_CACHE + articleId);
        SseEmitter emitter = new SseEmitter(0L);

        if (StringUtils.hasText(digestCache)) {
            try {
                emitter.send(SseEmitter.event().name("content").data(digestCache));
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        CompletionRequest req = buildDigestRequest(articleId, articleMapper, ossService);
        client.stream(req, new ChunkListener() {
            private SseChunkListener delegate;
            private String content = "";

            @Override
            public void onStart(StreamHandle handle) {
                delegate = new SseChunkListener(emitter, handle);
            }

            @Override
            public void onContent(String chunk) {
                delegate.onContent(chunk);
                if (!chunk.isEmpty()) content += chunk;
            }

            @Override
            public void onEnd(FinishReason finishReason) {
                delegate.onEnd(finishReason);
            }

            @Override
            public void onComplete() {
                delegate.onComplete();
                redisTemplate.opsForValue().set(RedisPrefix.ARTICLE_DIGEST_CACHE + articleId,
                        content, 7, TimeUnit.DAYS);
                log.info("文章: {}, 摘要生成完毕", articleId);
            }

            @Override
            public void onError(IOException e) {
                delegate.onError(e);
            }
        });

        return emitter;
    }

    @Override
    public SseEmitter generateAgentChat(AgentChatDTO dto) throws JsonProcessingException, InterruptedException {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        long callTimes = redisTemplate.opsForValue().increment(RedisPrefix.BLOG_AGENT_CHAT);
        if (callTimes == 1) redisTemplate.expire(RedisPrefix.BLOG_AGENT_CHAT, 10, TimeUnit.MINUTES);
        if (callTimes > 5) throw new BusinessException(BusErrorCode.AGENT_SERVICE_BUSY);

        Long id;
        String threadId = dto.getThreadId();
        String question = dto.getQuestion();
        if (question == null || question.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        final AgentState state;
        final AgentChat po;
        if (threadId == null) {
            id = IdGenerator.nextId();
            threadId = UUID.randomUUID().toString();
            state = AgentState.create(List.of(
                    new SystemMessage(BLOG_CHAT_PROMPT),
                    new HumanMessage(dto.getQuestion())
            ));
            state.setContext(new ToolContext(Map.of("userId", userId)));

            po = new AgentChat();
            po.setId(id);
            po.setUserId(userId);
            po.setThreadId(threadId);
        } else {
            po = agentChatMapper.selectByThreadId(threadId);
            if (po == null || !userId.equals(po.getUserId())) {
                throw new BusinessException(ResultCode.FAIL);
            }
            state = objectMapper.readValue(po.getState(), AgentState.class);
            state.getMessages().add(0, new SystemMessage(BLOG_CHAT_PROMPT));
            state.getMessages().add(new HumanMessage(question));
        }

        TokenTracker tokenTracker = new TokenTracker();
        state.setTracker(tokenTracker);

        ReentrantLock lock = lockFor(threadId);
        if (!lock.tryLock(0, TimeUnit.SECONDS)) {
            throw new BusinessException(BusErrorCode.AGENT_SERVICE_BUSY);
        }

        SseEmitter sseEmitter = new SseEmitter(0L);
        try {
            webChatAgent.stream(state, new ChunkListener() {
                private SseChunkListener delegate;
                private int totalTokens = 0;

                @Override
                public void onStart(StreamHandle handle) {
                    delegate = new SseChunkListener(sseEmitter, handle);
                }

                @Override
                public void onContent(String chunk) {
                    delegate.onContent(chunk);
                }

                @Override
                public void onReasoning(String reasoning) {
                    delegate.onReasoning(reasoning);
                }

                @Override
                public void onToolCall(ToolCall toolCall) {
                    delegate.onToolCall(toolCall);
                }

                @Override
                public void onEnd(FinishReason finishReason) {
                    delegate.onEnd(finishReason);
                }

                @Override
                public void onComplete() {
                    String threadId = po.getThreadId();
                    delegate.onCustom("thread", threadId);
                    delegate.onComplete();
                    for (TokenTracker.TokenUsage tokenUsage : tokenTracker.getUsageList()) {
                        totalTokens += tokenUsage.getTotalTokens();
                    }
                    log.info("[对话完成]共消耗token: {}", totalTokens);

                    try {
                        state.getMessages().removeIf(i -> i instanceof SystemMessage);
                        po.setState(objectMapper.writeValueAsString(state));
                        agentChatMapper.insertOrUpdate(po);
                    } catch (JsonProcessingException e) {
                        log.warn("[对话异常]序列化失败, threadId = {}", threadId);
                        throw new RuntimeException(e);
                    } finally {
                        lock.unlock();
                        cleanupLock(threadId);
                    }
                }

                @Override
                public void onError(IOException e) {
                    try {
                        log.error("[对话异常]流意外结束，原因：{}", e.getMessage());
                        delegate.onError(e);
                    } finally {
                        lock.unlock();
                        cleanupLock(po.getThreadId());
                    }
                }
            });
        } catch (Exception e) {
            lock.unlock();
            cleanupLock(threadId);
            throw e;
        }

        return sseEmitter;
    }

    public static CompletionRequest buildDigestRequest(Long articleId, ArticleMapper articleMapper, OssService ossService) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getKey() == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        String content = ossService.getArticleContent(article.getKey(), null);
        String type = article.articleType();
        String prompt = ARTICLE_DIGEST_PROMPT.formatted(
                type, article.getArticleName(),
                type.equals("技术") ? TECH_RULES : LIFE_RULES, content
        );
        return CompletionRequest.builder()
                .maxToken(300)
                .thinking(false)
                .toolChoice(ToolChoice.NONE) // 不允许任何工具请求
                .messages(List.of(
                        new SystemMessage(BlogSystemPrompt.SYSTEM_PROMPT),
                        new HumanMessage(prompt)
                ))
                .build();
    }

    private ReentrantLock lockFor(String threadId) {
        return locks.computeIfAbsent(threadId, k -> new ReentrantLock());
    }

    private void cleanupLock(String threadId) {
        locks.computeIfPresent(threadId, (k, lock) ->
                lock.isLocked() || lock.hasQueuedThreads() ? lock : null);
    }
}
