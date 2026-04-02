package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.vo.ArticleHistoryVO;
import io.github.oldmerman.web.service.ArticleHistoryService;
import lombok.RequiredArgsConstructor;
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
        return Result.success(service.getArticleHistory());
    }
}
