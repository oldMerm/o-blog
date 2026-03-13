package io.github.oldmerman.web.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.web.config.OssConfig;
import io.github.oldmerman.web.mapper.ArticleImageMapper;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.service.OssService;
import io.github.oldmerman.web.util.PathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description 文章相关的定时任务
 * 目前包括刷新文章内容and删除孤儿图片
 * @author oldmerman
 * @date 2026-3-13
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleRefresher {

    private final ArticleMapper articleMapper;

    private final ArticleImageMapper articleImageMapper;

    private final OssService ossService;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 1 ? * MON")
    public void refreshArticle() throws JsonProcessingException {
        log.info("执行定时任务 - 刷新文章内容 - {}", new Date());
        toRedisCache(Article.ArticleType.TECHNOLOGY);
        toRedisCache(Article.ArticleType.DAILY);
    }

    @Scheduled(cron = "0 0 1 ? * TUE")
    public void ArticleImageCleaner() {
        log.info("执行定时任务 - 删除文章未关联图片 - {}", new Date());
        List<String> keys = articleImageMapper.selectDanglingRecord();
        if(!keys.isEmpty()){
            ossService.deleteBatch(PathUtils.getOssPubKeys(keys), OssConfig.PUB_BUCKET);
        }
    }

    private void toRedisCache(byte type) throws JsonProcessingException {
        redisTemplate.opsForValue().set(RedisPrefix.ARTICLE_RENDER + type,
                objectMapper.writeValueAsString(articleMapper.selectArticle(type, 11L)),
                7, TimeUnit.DAYS);
    }
}
