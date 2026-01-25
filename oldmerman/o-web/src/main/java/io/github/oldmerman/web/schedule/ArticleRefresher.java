package io.github.oldmerman.web.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.web.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleRefresher {

    private final ArticleMapper articleMapper;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 1 ? * MON")
    public void refreshArticle() throws JsonProcessingException {
        log.info("执行任务 - 刷新文章内容 - {}", new Date());
        toRedisCache(Article.ArticleType.TECHNOLOGY);
        toRedisCache(Article.ArticleType.DAILY);
    }

    private void toRedisCache(byte type) throws JsonProcessingException {
        redisTemplate.opsForValue().set(RedisPrefix.ARTICLE_RENDER + type,
                objectMapper.writeValueAsString(articleMapper.selectArticle(type, 11L)),
                7, TimeUnit.DAYS);
    }
}
