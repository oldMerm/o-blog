package io.github.oldmerman.web.agent.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.core.api.chat.ChatClient;
import io.github.oldmerman.core.api.chat.model.CompletionResponse;
import io.github.oldmerman.core.api.chat.param.CompletionRequest;
import io.github.oldmerman.core.tool.Param;
import io.github.oldmerman.core.tool.Tool;
import io.github.oldmerman.core.tool.ToolContext;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.model.vo.ArticleHistoryVO;
import io.github.oldmerman.web.config.CorsConfig;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.service.ArticleHistoryService;
import io.github.oldmerman.web.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

import static io.github.oldmerman.web.service.impl.AiCompletionServiceImpl.buildDigestRequest;

@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleTools {

    private final ChatClient client;

    private final ArticleHistoryService articleHistoryService;

    private final OssService ossService;

    private final ArticleMapper articleMapper;

    @Qualifier("ignoreNullMapper")
    private final ObjectMapper objectMapper;

    private final StringRedisTemplate redisTemplate;

    private final CorsConfig config;

    @Tool(name = "get_article_page",
            description = "分页获取部分文章基本信息(唯一id，文章名，创建时间)，按时间降序")
    public List<Article> getPageArticle(@Param(name = "current", description = "当前页数") Integer current,
                                        @Param(name = "size", description = "每页大小") Integer size) throws JsonProcessingException {
        // 构建分页对象
        current = current == null ? 1 : current;
        size = (size == null ? 5 : size);
        Page<Article> page = new Page<>(current, size);
        log.info("[模型工具调用]get_article_page: current={}, size={}", current, size);

        // 构建查询wrapper
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Article::getId, Article::getArticleName, Article::getCreatedAt);
        wrapper.eq(Article::getArticleStatus, 3);
        wrapper.orderByDesc(Article::getCreatedAt);

        Page<Article> pageResult = articleMapper.selectPage(page, wrapper);
        List<Article> records = pageResult.getRecords();
        if (records.isEmpty()) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        return records;
    }

    @Tool(name = "get_article_url", description = "获取文章的公共访问链接(供用户访问)")
    public String getArticleUrl(@Param(name = "articleId", description = "文章唯一id") Long articleId) {
        try {
            log.info("[模型工具调用]get_article_url: articleId={}", articleId);
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST);
            }

            String format = "%s/markdown/%s?isPublic=public".formatted(config.getOrigins().split(",")[0], articleId);
            if (format == null || format.isBlank()) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST);
            }

            return format;
        } catch (Exception e) {
            log.error("[模型工具调用]get_article_url调用失败，失败原因：{}", e.getMessage());
            throw e;
        }
    }

    @Tool(name = "get_article_digest", description = "获取文章概述(通过子代理生成，若未命中缓存，耗时会比较长)")
    public String getArticleDigest(@Param(description = "文章唯一id") Long articleId) {
        // 从缓存中查询，减少模型消耗
        String cache = redisTemplate.opsForValue().get(RedisPrefix.ARTICLE_DIGEST_CACHE + articleId);
        if (StringUtils.hasText(cache)) {
            return cache;
        }
        // 调用摘要生成模型
        try {
            return generateArticleDigestSync(articleId);
        } catch (BusinessException be) {
            log.error("[模型工具调用]get_article_digest调用失败，失败原因：{}", be.getMessage());
            throw be;
        } catch (Exception e) {
            log.info("[模型工具调用]get_article_digest调用失败，失败原因：{}", e.getMessage());
            throw e;
        }
    }

    @Tool(name = "get_article_history",
            description = "获取用户近五条文章浏览历史")
    public String getArticleHistory(ToolContext ctx) throws JsonProcessingException {
        Long userId = ctx.get("userId", Long.class);
        if (userId == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        List<ArticleHistoryVO> historyVOList = articleHistoryService.getArticleHistory(userId);
        if (historyVOList.isEmpty()) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        List<ArticleHistoryVO> voList = historyVOList.stream().limit(5).toList();
        try {
            return objectMapper.writeValueAsString(voList);
        } catch (JsonProcessingException e) {
            log.error("[模型工具调用]get_article_history调用失败，序列化异常：{}", e.getMessage());
            throw e;
        }
    }

    private String generateArticleDigestSync(Long articleId) {
        CompletionRequest req = buildDigestRequest(articleId, articleMapper, ossService);
        CompletionResponse res = client.chat(req);
        return res.finalResponse();
    }
}
