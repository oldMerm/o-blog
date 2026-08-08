package io.github.oldmerman.web.service.impl;

import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KnowledgeRemoteServiceImplTest {

    @Autowired
    private WebClient webClient;

    @Test
    void health() {
        webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(data -> {
                    System.out.println("服务健康, "+data);
                })
                .doOnError(error -> {
                    System.out.println("[knowledge-agent]智能体服务出错");
                    throw new BusinessException(ResultCode.FAIL);
                })
                .block();
    }
}