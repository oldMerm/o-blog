package io.github.oldmerman.web.service;

import io.github.oldmerman.model.dto.ArticleGenDTO;
import reactor.core.publisher.Flux;

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
     *
     * @param dto 请求需要的参数
     * @return stream-chunk
     */
    Flux<String> forwardStream(ArticleGenDTO dto);

}
