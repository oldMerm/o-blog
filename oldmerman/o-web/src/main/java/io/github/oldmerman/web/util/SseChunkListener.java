package io.github.oldmerman.web.util;

import io.github.oldmerman.core.DeepseekClient;
import io.github.oldmerman.core.enums.FinishReason;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 流式适配器
 * @author oldmerman and deepseek
 * @date 2026-10-13
 */
public class SseChunkListener implements DeepseekClient.ChunkListener {

    private final SseEmitter emitter;
    private final DeepseekClient.StreamHandle handle;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public SseChunkListener(SseEmitter emitter, DeepseekClient.StreamHandle handle) {
        this.emitter = emitter;
        this.handle = handle;

        // 前端断开 / 超时 / 完成 → 取消上游 DeepSeek 请求
        emitter.onCompletion(this::cancelUpstream);
        emitter.onTimeout(() -> { cancelUpstream(); emitter.complete(); });
        emitter.onError(e -> cancelUpstream());
    }

    private void cancelUpstream() {
        if (closed.compareAndSet(false, true) && handle != null) {
            handle.cancel();
        }
    }

    @Override
    public void onStart(DeepseekClient.StreamHandle h) {}

    @Override
    public void onContent(String chunk) {
        send("content", chunk);
    }

    @Override
    public void onReasoning(String reasoning) {
        send("reasoning", reasoning);
    }


    @Override
    public void onEnd(FinishReason finishReason) {
        send("end", finishReason.name());
    }

    @Override
    public void onComplete() {
        if (closed.compareAndSet(false, true)) {
            try {
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            } catch (IOException ignored) {
            }
            emitter.complete();
        }
    }

    @Override
    public void onError(IOException e) {
        if (closed.compareAndSet(false, true)) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(e.getMessage() == null ? "unknown" : e.getMessage()));
            } catch (IOException ignored) {
            }
            emitter.completeWithError(e);
        }
    }

    private void send(String event, String data) {
        if (closed.get()) return;
        if (data.isEmpty()) {
            return;
        }
        try {
            String safe = data.replace("\r", "").replace("\n", "\\n");
            emitter.send(SseEmitter.event().name(event).data(safe));
        } catch (IOException e) {
            cancelUpstream();
            emitter.completeWithError(e);
        }
    }
}