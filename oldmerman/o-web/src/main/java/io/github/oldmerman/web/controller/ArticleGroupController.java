package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.vo.ArticleGroupRenderVO;
import io.github.oldmerman.web.service.ArticleGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article/group")
@RequiredArgsConstructor
public class ArticleGroupController {

    private final ArticleGroupService service;

    @GetMapping
    public Result<List<ArticleGroupRenderVO>> getRenderGroup(){
        return Result.success(service.getRenderGroup());
    }

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

    @PostMapping("/unlink")
    public Result<Void> unlinkArticleInGroup(@RequestParam Long articleId,
                                             @RequestParam Long groupId){
        service.unlinkArticleInGroup(articleId, groupId);
        return Result.success();
    }

}
