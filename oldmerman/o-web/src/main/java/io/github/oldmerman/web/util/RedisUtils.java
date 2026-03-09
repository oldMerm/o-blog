package io.github.oldmerman.web.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.enums.NumEnum;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis通用工具类
 *
 * @author oldmerman
 * @date 2026-3-9
 */
public class RedisUtils {

    /**
     * 文章缓存重构
     *
     * @param articleType 文章缓存类型
     * @param articleId   文章id
     * @throws JsonProcessingException 序列化失败
     */
    public static void rebuildArticleRenderCache(Byte articleType, Long articleId,
                                          StringRedisTemplate redisTemplate, ObjectMapper objectMapper) throws JsonProcessingException {
        String data = redisTemplate.opsForValue().get(RedisPrefix.ARTICLE_RENDER + articleType);
        if (!ObjectUtils.isEmpty(data)) {
            List<ArticleRenderVO> renderList = objectMapper.readValue(data, new TypeReference<>() {
            });
            renderList.removeIf(articleRenderVO -> articleRenderVO.getId().equals(articleId.toString()));
            redisTemplate.opsForValue().set(RedisPrefix.ARTICLE_RENDER + articleType, objectMapper.writeValueAsString(renderList),
                    NumEnum.ARTICLE_EXPIRE_TIME.getValue(), TimeUnit.DAYS);
        }
    }
}
