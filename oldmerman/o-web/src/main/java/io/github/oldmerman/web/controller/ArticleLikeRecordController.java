package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.web.service.ArticleLikeRecordService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article/like")
@RequiredArgsConstructor
public class ArticleLikeRecordController {

    private final ArticleLikeRecordService service;

    @GetMapping("{articleId}")
    public Result<Boolean> getLikeRecord(@PathVariable Long articleId){
        Long userId = UserContext.getUserId();
        return Result.success(service.getLikeRecord(userId, articleId));
    }

    @PostMapping("{articleId}")
    public Result<Void> saveLikeRecord(@PathVariable Long articleId){
        Long userId = UserContext.getUserId();
        service.saveLikeRecord(userId, articleId);
        return Result.success();
    }
}
