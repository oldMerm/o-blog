package io.github.oldmerman.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleGroupRenderVO {

    private Long id;

    private String groupName;

    private LocalDateTime createdAt;

    private Integer number;

}
