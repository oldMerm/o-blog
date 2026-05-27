package io.github.oldmerman.web.service;

/**
 * 文章分组相关接口
 *
 * @author oldmerman
 * @date 2026-5-27
 */

public interface ArticleGroupService {

    /**
     * 新增文章分组
     *
     * @param groupName 分组名称
     * @param groupDesc 分组描述
     */
    void insertArticleGroup(String groupName, String groupDesc);

    /**
     * 关联文章与文章分组
     *
     * @param articleId 文章id
     * @param groupId 分组id
     */
    void insertArticleInGroup(Long articleId, Long groupId);
}
