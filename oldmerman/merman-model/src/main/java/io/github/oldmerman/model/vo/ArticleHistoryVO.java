package io.github.oldmerman.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleHistoryVO {

    private String userId;

    private String articleId;

    private String articleName;

    private LocalDateTime updatedAt;

}
