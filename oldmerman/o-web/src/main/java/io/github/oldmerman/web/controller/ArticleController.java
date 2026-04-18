package io.github.oldmerman.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.ArticleCreateDTO;
import io.github.oldmerman.model.vo.ArticleInfoVO;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import io.github.oldmerman.web.service.ArticleService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("article")
@Slf4j
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    private final StringRedisTemplate redisTemplate;

    @GetMapping("/info")
    public Result<List<ArticleRenderVO>> info(){
        return Result.success(articleService.info());
    }

    @GetMapping("/public/info")
    public Result<List<ArticleRenderVO>> getRenderArticle(@RequestParam(name = "id", defaultValue = "1") Byte articleType,
                                                          @RequestParam(name = "size", defaultValue = "10") Long size) throws JsonProcessingException {
        return Result.success(articleService.getRenderArticle(articleType, size));
    }

    @GetMapping("/private/{articleId}")
    public Result<String> getPrivateArticleById(@PathVariable String articleId){
        return Result.success(articleService.getPrivateArticleById(Long.parseLong(articleId)));
    }

    @GetMapping("/public/{articleId}")
    public Result<ArticleInfoVO> getPublicArticleById(@PathVariable Long articleId){
        return Result.success(articleService.getPublicArticleById(articleId));
    }

    @PostMapping("upload/img")
    public Result<List<String>> uploadImagesToOSS(@RequestParam("paths") List<String> paths,
                                                  @RequestParam("files") List<MultipartFile> files){
        Long userId = UserContext.getUserId();
        Long expire = redisTemplate.getExpire(RedisPrefix.ARTICLE_SUBMIT + userId, TimeUnit.MINUTES);
        if(expire > 0){
            throw new BusinessException(BusErrorCode.ARTICLE_SUBMIT_FREQUENT.getCode(), "上传过于频繁，请"+expire+"分钟后尝试");
        }
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

    @DeleteMapping("remove/{articleName}")
    public Result<Void> removeArticle(@PathVariable String articleName) throws JsonProcessingException {
        Long userId = UserContext.getUserId();
        log.info("用户删除文章:{}", articleName);
        articleService.removeArticle(articleName, userId);
        return Result.success();
    }
}
