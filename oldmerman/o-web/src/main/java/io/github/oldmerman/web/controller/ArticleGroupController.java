package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.web.service.ArticleGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article/group")
@RequiredArgsConstructor
public class ArticleGroupController {

    private final ArticleGroupService service;

    @PostMapping
    public Result<Void> insertArticleGroup(@RequestParam String groupName,
                                           @RequestParam String groupDesc){
        service.insertArticleGroup(groupName, groupDesc);
        return Result.success();
    }

    @PostMapping("/link")
    public Result<Void> insertArticleInGroup(@RequestParam Long articleId,
                                             @RequestParam Long groupId){
        service.insertArticleInGroup(articleId, groupId);
        return Result.success();
    }

}
