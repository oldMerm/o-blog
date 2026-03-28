package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.AiMessagesDTO;
import io.github.oldmerman.model.po.AiConversation;
import io.github.oldmerman.model.vo.AiConversationVO;
import io.github.oldmerman.model.vo.AiMessagesVO;
import io.github.oldmerman.web.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/session")
    public Result<AiConversationVO> createSession(){
        return Result.success(service.createSession());
    }

    @PostMapping("/chat")
    public Mono<Result<AiMessagesVO>> chat(@RequestBody AiMessagesDTO dto){
        return service.chat(dto);
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
