package io.github.oldmerman.web.agent.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.core.tool.Param;
import io.github.oldmerman.core.tool.Tool;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ArticleTools {

    @Autowired
    private OssService ossService;

    @Autowired
    private ArticleMapper mapper;

    @Autowired
    @Qualifier("ignoreNullMapper")
    private ObjectMapper objectMapper;

    @Tool(name = "get_all_article",
            description = "分页获取部分文章基本信息(唯一id，文章名，文章简介，创建时间)，按时间降序")
    public String getPageArticle(@Param(description = "当前页数") Integer current,
                                 @Param(description = "每页大小") Integer size) throws JsonProcessingException {
        // 构建分页对象
        current = current == null ? 1 : current;
        size = (size == null ? 5 : size);
        Page<Article> page = new Page<>(current, size);
        log.info("[模型工具调用]get_all_article: current={}, size={}", current, size);

        // 构建查询wrapper
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Article::getId, Article::getArticleName, Article::getArticleDecr, Article::getCreatedAt);
        wrapper.eq(Article::getArticleStatus, 3);
        wrapper.orderByDesc(Article::getCreatedAt);

        Page<Article> pageResult = mapper.selectPage(page, wrapper);
        List<Article> records = pageResult.getRecords();
        if (records.isEmpty()) {
            return "{\"error\": \"[业务异常]数据不存在, current=" + current + "，size=" + size + "\"}";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("records", records);
        map.put("current", pageResult.getCurrent());
        map.put("size", pageResult.getSize());
        map.put("total", pageResult.getTotal());
        return objectMapper.writeValueAsString(map);
    }

    @Tool(name = "get_article_content", description = "根据id获取文章内容")
    public String getArticleContent(@Param(description = "文章唯一ID") Long articleId) {
        try {
            log.info("[模型工具调用]get_article_content: articleId={}", articleId);
            Article article = mapper.selectById(articleId);
            if (article == null || article.getKey() == null) {
                return "[业务异常]不存在文章，id：" + articleId;
            }

            String content = ossService.getArticleContent(article.getKey(), null);
            if (content == null || content.isBlank()) {
                return "{\"error\": \"[业务异常]不存在文章，id：" + articleId + "\"}";
            }

            return content;
        } catch (Exception e) {
            log.error("[模型工具调用]get_article_content调用失败，失败原因：{}", e.getMessage());
            return "{\"error\": \"[系统异常]后台系统异常，服务不可用...\"}";
        }
    }

}
