package io.github.oldmerman.web.service;

import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.model.po.ArticleSearch;

import java.util.List;

/**
 * 文章搜索相关接口
 * @author oldmerman
 * @date 2026-11-23
 */
public interface ArticleSearchService {

    /**
     * 文章搜索
     * @param keyword 搜索关键字
     * @return 分页结果，文章名以及文章描述
     */
    List<ArticleSearch> search(String keyword);

    /**
     * 同步文章搜索表
     */
    void refreshSearchArticle(boolean isUpdated, Article article);

}
