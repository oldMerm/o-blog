package io.github.oldmerman.web.service;

import io.github.oldmerman.model.vo.ArticleHistoryVO;

import java.util.List;

/**
 * 文章浏览历史相关接口
 * @author oldmerman
 * @date 2026-4-1
 */
public interface ArticleHistoryService {

    /**
     * 获取文章浏览历史
     *
     * @return 浏览历史
     */
    List<ArticleHistoryVO> getArticleHistory();

}
