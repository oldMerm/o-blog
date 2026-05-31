package io.github.oldmerman.web.service;

import io.github.oldmerman.model.vo.ArticleGroupRenderVO;

import java.util.List;

/**
 * 文章分组相关接口
 *
 * @author oldmerman
 * @date 2026-5-27
 */

public interface ArticleGroupService {

    /**
     * 获取分组信息
     *
     * @return 分组渲染信息
     */
    List<ArticleGroupRenderVO> getRenderGroup();

    /**
     * 新增文章分组
     *
     * @param groupName 分组名称
     * @param groupDesc 分组描述
     */
    void insertArticleGroup(String groupName, String groupDesc);

    /**
     * 删除文章分组
     *
     * @param groupId 分组id
     */
    void removeArticleGroup(Long groupId);

    /**
     * 关联文章与文章分组
     *
     * @param articleId 文章id
     * @param groupId   分组id
     */
    void insertArticleInGroup(Long articleId, Long groupId);

    /**
     * 解除分组和文章的关联
     *
     * @param articleId 文章id
     * @param groupId   分组id
     */
    void unlinkArticleInGroup(Long articleId, Long groupId);

}
