package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.ArticleCreateDTO;
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

    @GetMapping("info")
    public Result<List<ArticleRenderVO>> info(){
        log.info("用户查询个人文章信息");
        return Result.success(articleService.info());
    }

    @GetMapping("private")
    public Result<String> getPrivateArticleById(@RequestParam("articleId") String id){
        log.info("查询文章:{}",id);
        return Result.success(articleService.getPrivateArticleById(Long.parseLong(id)));
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

}
