package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.dto.ArticleAdminUpdateDTO;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.model.vo.ArticlePageVO;
import io.github.oldmerman.web.converter.ArticleConverter;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.service.ArticleAdminService;
import io.github.oldmerman.web.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleAdminServiceImpl implements ArticleAdminService {

    private final ArticleMapper articleMapper;

    private final ArticleConverter converter;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    /**
     * 分页查询文章信息
     *
     * @param current 起始页
     * @param size    每页大小
     * @return 统一封装对象
     */
    public PageResult<ArticlePageVO> page(Long current, Long size) {
        Page<Article> page = new Page<>(current, size);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreatedAt);
        IPage<Article> iPage = articleMapper.selectPage(page, queryWrapper);
        List<ArticlePageVO> list = iPage.getRecords().stream()
                .map(converter::poToPageVO)
                .toList();
        return PageResult.of(
                iPage.getCurrent(),
                iPage.getSize(),
                iPage.getTotal(),
                list
        );
    }

    /**
     * 修改文章状态
     *
     * @param dto 封装对象
     */
    public void updateArticleStatus(ArticleAdminUpdateDTO dto) throws JsonProcessingException {
        Long id = dto.getId();
        Article article = articleMapper.selectById(id);
        LambdaUpdateWrapper<Article> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Article::getId, id).set(Article::getArticleStatus, dto.getStatus());
        articleMapper.update(wrapper);
        RedisUtils.rebuildArticleRenderCache(article.getArticleType(), id, redisTemplate, objectMapper);
    }


}
