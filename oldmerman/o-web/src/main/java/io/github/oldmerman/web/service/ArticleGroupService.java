package io.github.oldmerman.web.service;

import io.github.oldmerman.model.dto.ArticleGroupCreateDTO;
import io.github.oldmerman.model.vo.ArticleGroupRenderVO;
import io.github.oldmerman.model.vo.ArticleRenderVO;

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
     * 根据集合id获取文章信息
     *
     * @param groupId 集合id
     * @return 文章渲染信息
     */
    List<ArticleRenderVO> getArticleByGroup(Long groupId);

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
     * @param dto 封装对象
     */
    void insertArticleInGroup(ArticleGroupCreateDTO dto);

    /**
     * 解除分组和文章的关联
     *
     * @param articleId 文章id
     * @param groupId   分组id
     */
    void unlinkArticleInGroup(Long articleId, Long groupId);

}
