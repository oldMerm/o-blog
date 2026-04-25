package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.AiMessagesDTO;
import io.github.oldmerman.model.vo.AiConversationVO;
import io.github.oldmerman.model.vo.AiMessagesVO;
import io.github.oldmerman.web.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AiController {

    private final AiService service;

    @GetMapping
    public Result<List<AiConversationVO>> getSessions(){
        return Result.success(service.getSessions());
    }

    @GetMapping("/chatInfo")
    public Result<List<AiMessagesVO>> getChatInfo(@RequestParam("sessionId") String sessionId){
        return Result.success(service.getChatInfo(sessionId));
    }

    @GetMapping("/health")
    public Mono<Result<Integer>> health(){
        return service.health();
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@RequestParam("sessionId") String sessionId,
                                                @RequestParam("message") String content,
                                                @RequestParam("token") String token){
        return service.stream(sessionId, content, token);
    }

    @PostMapping("/session")
    public Result<AiConversationVO> createSession(){
        return Result.success(service.createSession());
    }

    @DeleteMapping
    public Result<Void> deleteOneSession(@RequestParam("sessionId") String sessionId){
        service.deleteOneSession(sessionId);
        return Result.success();
    }

    @DeleteMapping("/all")
    public Result<Void> deleteAllSessions(){
        service.deleteAllSession();
        return Result.success();
    }
}
