package io.github.oldmerman.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.dto.ArticleAdminUpdateDTO;
import io.github.oldmerman.model.vo.ArticlePageVO;

public interface ArticleAdminService {

    PageResult<ArticlePageVO> page(Long current, Long size);

    void updateArticleStatus(ArticleAdminUpdateDTO dto) throws JsonProcessingException;

}
