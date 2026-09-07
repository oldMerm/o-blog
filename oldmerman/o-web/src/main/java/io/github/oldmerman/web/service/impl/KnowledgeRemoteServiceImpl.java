package io.github.oldmerman.web.service.impl;

import cn.hutool.json.JSONUtil;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.model.remote.ArticleGenDTO;
import io.github.oldmerman.web.config.KnowledgeRemoteClientConfig;
import io.github.oldmerman.web.service.KnowledgeRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
                    log.error("智能体服务出错, {}", error.getMessage());
                    throw new BusinessException(BusErrorCode.AGENT_SERVICE_FAILED);
                })
                .block();
    }

    @Override
    public Flux<String> forwardStream(ArticleGenDTO dto) {
        return webClient.post()
                .uri("/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("api-key", config.getKey())
                .bodyValue(JSONUtil.toJsonStr(dto))
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(error -> {
                    log.error("智能体服务出错, {}", error.getMessage());
                    throw new BusinessException(BusErrorCode.AGENT_SERVICE_FAILED);
                });
    }

    @Override
    public Mono<String> generateAgentLogSummary(String logText) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/log")
                        .queryParam("content", logText)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_HTML)
                .header("api-key", config.getKey())
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> {
                    log.error("智能体智能日志生成出错, {}", error.getMessage());
                    throw new BusinessException(BusErrorCode.AGENT_SERVICE_FAILED);
                });
    }
}
