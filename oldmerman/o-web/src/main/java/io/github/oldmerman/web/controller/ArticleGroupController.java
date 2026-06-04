package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.ArticleGroupCreateDTO;
import io.github.oldmerman.model.vo.ArticleGroupRenderVO;
import io.github.oldmerman.model.vo.ArticleRenderVO;
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

    @GetMapping("/public/{groupId}")
    public Result<List<ArticleRenderVO>> getArticleByGroup(@PathVariable Long groupId){
        return Result.success(service.getArticleByGroup(groupId));
    }

    @GetMapping("/public/group_info/{articleId}")
    public Result<List<ArticleGroupRenderVO>> getGroupByArticle(@PathVariable Long articleId){
        return Result.success(service.getGroupArticle(articleId));
    }

    @PostMapping
    public Result<Void> insertArticleGroup(@RequestParam String groupName,
                                           @RequestParam String groupDesc){
        service.insertArticleGroup(groupName, groupDesc);
        return Result.success();
    }

    @DeleteMapping("/{groupId}")
    public Result<Void> removeArticleGroup(@PathVariable Long groupId){
        service.removeArticleGroup(groupId);
        return Result.success();
    }

    @PostMapping("/link")
    public Result<Void> insertArticleInGroup(@RequestBody ArticleGroupCreateDTO dto){
        service.insertArticleInGroup(dto);
        return Result.success();
    }

    @DeleteMapping("/unlink/{groupId}/{articleId}")
    public Result<Void> unlinkArticleInGroup(@PathVariable Long articleId,
                                             @PathVariable Long groupId){
        service.unlinkArticleInGroup(articleId, groupId);
        return Result.success();
    }

}
