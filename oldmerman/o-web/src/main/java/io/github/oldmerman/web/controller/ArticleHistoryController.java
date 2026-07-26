package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.vo.ArticleHistoryVO;
import io.github.oldmerman.web.service.ArticleHistoryService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/article/history")
@RequiredArgsConstructor
public class ArticleHistoryController {

    private final ArticleHistoryService service;

    @GetMapping
    public Result<List<ArticleHistoryVO>> getArticleHistory(){
        return Result.success(service.getArticleHistory(UserContext.getUserId()));
    }

    @DeleteMapping
    public Result<Void> removeArticleHistory(){
        service.removeArticleHistory(UserContext.getUserId());
        return Result.success();
    }
}
