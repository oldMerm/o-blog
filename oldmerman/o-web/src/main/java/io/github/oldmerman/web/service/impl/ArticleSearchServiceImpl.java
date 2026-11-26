package io.github.oldmerman.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.model.po.ArticleSearch;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.mapper.ArticleSearchMapper;
import io.github.oldmerman.web.service.ArticleSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleSearchServiceImpl implements ArticleSearchService {

    private final ArticleSearchMapper mapper;

    private final ArticleMapper articleMapper;

    @Override
    public List<ArticleSearch> search(String keyword) {
        List<ArticleSearch> list = mapper.search(keyword);
        return list.isEmpty() ? Collections.emptyList() : list;
    }

    @Override
    public void refreshSearchArticle(boolean isUpdated, Article article) {
        if (isUpdated) {
            if (BeanUtil.isEmpty(article)) {
                throw new BusinessException(BusErrorCode.FILE_LEN_FAILED, "传入文章为空，搜索表更新失败");
            }
            mapper.insert(articleToSearch(article));
            return;
        }

        log.info("同步文章表至搜索表...");
        int current = 1;
        int size = 50;
        while (true) {
            List<Article> articleList = articleMapper.selectLimit((current - 1) * size, size);
            mapper.insertOrIgnoreBatch(articleList.stream().map(this::articleToSearch).toList());
            if (articleList.size() <= size) {
                break;
            }
            log.info("进行第 {} 次同步，{}", ++current, LocalDateTime.now());
        }

    }

    private ArticleSearch articleToSearch(Article article){
        ArticleSearch po = new ArticleSearch();
        po.setArticleId(article.getId());
        po.setTitle(article.getArticleName());
        po.setSummary(article.getArticleDecr());
        return po;
    }
}
