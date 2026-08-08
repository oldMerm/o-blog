package io.github.oldmerman.web.service.impl;

import cn.hutool.json.JSONUtil;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.model.dto.ArticleGenDTO;
import io.github.oldmerman.web.config.KnowledgeRemoteClientConfig;
import io.github.oldmerman.web.service.KnowledgeRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeRemoteServiceImpl implements KnowledgeRemoteService {

    private final KnowledgeRemoteClientConfig config;

    private final WebClient webClient;

    @Override
    public void health() {
        webClient.get()
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> {
                    log.error("[knowledge-agent]智能体服务出错, {}", error.getMessage());
                    throw new BusinessException(ResultCode.FAIL);
                })
                .block();
    }

    @Override
    public Flux<String> forwardStream(ArticleGenDTO dto) {
        String key = config.getKey();
        return webClient.post()
                .uri("/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("api-key", key)
                .bodyValue(JSONUtil.toJsonStr(dto))
                .exchangeToFlux(response -> response.bodyToFlux(String.class))
                .map(line -> line)
                .doOnError(error -> log.error("SSE传输出错, {}", error.getMessage()));
    }
}
