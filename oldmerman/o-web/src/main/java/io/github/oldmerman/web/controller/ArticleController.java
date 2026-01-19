package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.ArticleCreateDTO;
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

    @PostMapping("upload/img")
    public Result<List<String>> uploadImagesToOSS(@RequestParam("paths") List<String> paths,
                                                  @RequestParam("files") List<MultipartFile> files){
        Long userId = UserContext.getUserId();
        log.info("用户{},上传图片",userId);
        return Result.success(articleService.uploadImagesToOSS(userId, paths, files));
    }

    @PostMapping("upload")
    public Result<Void> upload(@RequestParam("md") MultipartFile file,
                               @RequestBody ArticleCreateDTO dto){
        Long userId = UserContext.getUserId();
        log.info("用户:{},上传markdown文档",userId);
        articleService.upload(userId, file, dto);
        return Result.success();
    }
}
