package io.github.oldmerman.web.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * ai调用接口
 * @author oldmerman
 * @date 2026-10-13
 */
public interface AiCompletionService {

    /**
     * 文章摘要生成接口
     * @param articleId 文章唯一id
     */
    SseEmitter generateArticleDigest(Long articleId);
}
