package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.po.AiConversation;
import io.github.oldmerman.web.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AiController {

    private final AiService service;

    @GetMapping
    public Result<List<AiConversation>> getSessions(){
        return Result.success(service.getSessions());
    }

    @PostMapping("/session")
    public Result<Void> createSession(){
        service.createSession();
        return Result.success();
    }
}
