package io.github.oldmerman.web.controller;

import io.github.oldmerman.web.service.AiCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/chat/completion")
@RequiredArgsConstructor
public class AiCompletionController {

    private final AiCompletionService service;

    @PostMapping(value = "/{articleId}/digest", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateArticleDigest(@PathVariable("articleId") Long articleId){
        return service.generateArticleDigest(articleId);
    }
}
