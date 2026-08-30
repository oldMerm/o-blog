package io.github.oldmerman.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.oldmerman.model.remote.ArticleGenDTO;
import io.github.oldmerman.model.remote.LogSummaryGenDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * 鱼人知识库远程服务相关接口
 * @author oldmerman
 * @date 2026-8-6
 */
public interface KnowledgeRemoteService {

    /**
     * 服务心跳测试
     */
    void health();

    /**
     * 中转智能体服务的SSE流
     * @param dto 请求需要的参数
     * @return stream-chunk
     */
    Flux<String> forwardStream(ArticleGenDTO dto) throws JsonProcessingException;

    /**
     * AI日志报告
     */
    Mono<String> generateAgentLogSummary(String path) throws IOException;
}
