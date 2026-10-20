package io.github.oldmerman.web.service.impl;

import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.core.api.chat.ChatClient;
import io.github.oldmerman.core.api.chat.messages.HumanMessage;
import io.github.oldmerman.core.api.chat.param.CompletionRequest;
import io.github.oldmerman.core.enums.FinishReason;
import io.github.oldmerman.core.enums.ToolChoice;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.service.AiCompletionService;
import io.github.oldmerman.web.service.OssService;
import io.github.oldmerman.web.util.SseChunkListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.oldmerman.common.constant.ArticleDigestPrompt.ARTICLE_DIGEST_PROMPT;
import static io.github.oldmerman.common.constant.ArticleDigestPrompt.TECH_RULES;
import static io.github.oldmerman.common.constant.ArticleDigestPrompt.LIFE_RULES;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiCompletionServiceImpl implements AiCompletionService {

    private final ChatClient client;

    private final OssService ossService;

    private final ArticleMapper articleMapper;

    private final StringRedisTemplate redisTemplate;

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

        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getKey() == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        String content = ossService.getArticleContent(article.getKey(), null);
        String type = article.articleType();
        String prompt = ARTICLE_DIGEST_PROMPT.formatted(
                type, article.getArticleName(), content,
                type.equals("技术") ? TECH_RULES : LIFE_RULES
        );
        CompletionRequest req = CompletionRequest.builder()
                .maxToken(300)
                .thinking(false)
                .toolChoice(ToolChoice.NONE) // 不允许任何工具请求
                .messages(List.of(new HumanMessage(prompt)))
                .build();

        client.stream(req, new ChatClient.ChunkListener() {
            private SseChunkListener delegate;
            private String content = "";

            @Override
            public void onStart(ChatClient.StreamHandle handle) {
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
}
