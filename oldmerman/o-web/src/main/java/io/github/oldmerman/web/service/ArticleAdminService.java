package io.github.oldmerman.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.dto.ArticleAdminUpdateDTO;
import io.github.oldmerman.model.vo.ArticlePageVO;

/**
 * 管理端相关接口
 * @author oldmerman
 * @date 2026-2-21
 */
public interface ArticleAdminService {

    /**
     * 分页查询文章信息
     *
     * @param current 起始页
     * @param size    每页大小
     * @return 统一封装对象
     */
    PageResult<ArticlePageVO> page(Long current, Long size);

    /**
     * 修改文章状态
     *
     * @param dto 封装对象
     */
    void updateArticleStatus(ArticleAdminUpdateDTO dto) throws JsonProcessingException;

}
