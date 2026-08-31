package io.github.oldmerman.web.schedule;

import io.github.oldmerman.web.config.LogProperties;
import io.github.oldmerman.web.service.KnowledgeRemoteService;
import io.github.oldmerman.web.util.CompressUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * AI日志分析定时任务
 * @author oldmerman
 * @date 2026-8-31
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AILogGenerateTask {

    private final KnowledgeRemoteService remoteService;

    private final LogProperties properties;

    /**
     * 执行时间：每周一早八点
     */
    @Scheduled(cron = "0 0 8 ? * MON")
    public void generateAiLogOrSendEmail() throws IOException {
        String logText = CompressUtils.readLastWeekLog(properties.getPath() + "\\info");
        Mono<String> stringMono = remoteService.generateAgentLogSummary(logText);
    }
}
