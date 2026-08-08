package io.github.oldmerman.model.dto;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

@Data
public class ArticleGenDTO {

    @Alias("article_id")
    private String articleId;

    @Alias("article_name")
    private String articleName;

    private String content;

    @Alias("model_id")
    private String modelId;

}
