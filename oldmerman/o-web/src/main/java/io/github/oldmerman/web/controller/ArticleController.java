package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.web.service.ArticleService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("article")
@Slf4j
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("upload")
    public Result<List<String>> uploadImagesToOSS(@RequestParam("paths") List<String> paths,
                                                  @RequestParam("files") List<MultipartFile> files){
        Long userId = UserContext.getUserId();
        log.info("用户{},上传图片",userId);
        return Result.success(articleService.uploadImagesToOSS(userId, paths, files));
    }
}
