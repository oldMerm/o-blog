package io.github.oldmerman.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticlePageVO {

    private String id;

    private String articleName;

    private String articleWriter;

    private Byte articleType;

    private Byte articleStatus;

    private LocalDateTime createdAt;
}
