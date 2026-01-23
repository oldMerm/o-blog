package io.github.oldmerman.model.vo;

import lombok.Data;

@Data
public class ArticleRenderVO {

    private String id;

    private String articleName;

    private Byte articleStatus;

    private Integer like;

    private String createdAt;
}
