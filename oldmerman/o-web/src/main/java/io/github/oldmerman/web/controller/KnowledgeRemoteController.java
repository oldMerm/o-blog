package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.ArticleGenDTO;
import io.github.oldmerman.web.service.KnowledgeRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("knowledge-agent")
@RequiredArgsConstructor
public class KnowledgeRemoteController {

    private final KnowledgeRemoteService service;

    @GetMapping
    public Result<Void> health(){
        service.health();
        return Result.success();
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> forwardStream(@RequestBody ArticleGenDTO dto){
        return service.forwardStream(dto);
    }

}
