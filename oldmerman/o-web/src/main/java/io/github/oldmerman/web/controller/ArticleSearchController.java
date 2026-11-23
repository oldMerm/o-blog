package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.po.ArticleSearch;
import io.github.oldmerman.web.service.ArticleSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article/search")
@RequiredArgsConstructor
public class ArticleSearchController {

    private final ArticleSearchService service;

    @GetMapping("/{keyword}")
    public Result<List<ArticleSearch>> search(@PathVariable("keyword") String keyword) {
        return Result.success(service.search(keyword));
    }

    @PostMapping("/sync")
    public Result<Void> refreshSearchArticle(){
        service.refreshSearchArticle(false, null);
        return Result.success();
    }
}
