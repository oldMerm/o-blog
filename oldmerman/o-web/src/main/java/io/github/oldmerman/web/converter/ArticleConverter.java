package io.github.oldmerman.web.converter;

import io.github.oldmerman.model.dto.ArticleCreateDTO;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticleConverter {

    Article createToPO(ArticleCreateDTO dto);

    ArticleRenderVO poToRenderVO(Article article);
}
