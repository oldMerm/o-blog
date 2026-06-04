package io.github.oldmerman.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.ArticleCreateDTO;
import io.github.oldmerman.model.vo.ArticleInfoVO;
import io.github.oldmerman.model.vo.ArticlePageDetailVO;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import io.github.oldmerman.web.service.ArticleService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("article")
@Slf4j
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/info")
    public Result<List<ArticleRenderVO>> info(){
        return Result.success(articleService.info());
    }

    @GetMapping("/public/page")
    public Result<PageResult<ArticlePageDetailVO>> page(@RequestParam(name = "current", defaultValue = "1") Long current,
                                                        @RequestParam(name = "size", defaultValue = "6") Long size,
                                                        @RequestParam(name = "type", defaultValue = "1") Byte articleType){
        return Result.success(articleService.page(current, size, articleType));
    }

    @GetMapping("/public/info")
    public Result<List<ArticleRenderVO>> getRenderArticle(@RequestParam(name = "id", defaultValue = "1") Byte articleType,
                                                          @RequestParam(name = "size", defaultValue = "10") Long size) throws JsonProcessingException {
        return Result.success(articleService.getRenderArticle(articleType, size));
    }

    @GetMapping("/private/{articleId}")
    public Result<String> getPrivateArticleById(@PathVariable Long articleId){
        return Result.success(articleService.getPrivateArticleById(articleId));
    }

    @GetMapping("/public/{articleId}")
    public Result<ArticleInfoVO> getPublicArticleById(@PathVariable Long articleId){
        return Result.success(articleService.getPublicArticleById(articleId));
    }

    @PostMapping("upload/img")
    public Result<List<String>> uploadImagesToOSS(@RequestParam("paths") List<String> paths,
                                                  @RequestParam("files") List<MultipartFile> files){
        Long userId = UserContext.getUserId();
        log.info("用户{},上传图片",userId);
        return Result.success(articleService.uploadImagesToOSS(userId, paths, files));
    }

    @PostMapping("upload")
    public Result<Void> upload(@RequestParam("md") MultipartFile file,
                               @RequestParam("articleName") String articleName,
                               @RequestParam("articleDecr") String articleDecr,
                               @RequestParam("articleType") Byte articleType,
                               @RequestParam(value = "attrs", required = false) List<String> attrs){
        Long userId = UserContext.getUserId();
        ArticleCreateDTO dto = new ArticleCreateDTO();
        dto.setArticleName(articleName);
        dto.setArticleType(articleType);
        dto.setArticleDecr(articleDecr);
        dto.setAttrs(attrs);
        log.info("用户:{},上传markdown文档",userId);
        articleService.upload(userId, file, dto);
        return Result.success();
    }

    @DeleteMapping("remove/{articleId}")
    public Result<Void> removeArticle(@PathVariable Long articleId) throws JsonProcessingException {
        Long userId = UserContext.getUserId();
        log.info("用户删除文章:{}", articleId);
        articleService.removeArticle(articleId, userId);
        return Result.success();
    }
}
