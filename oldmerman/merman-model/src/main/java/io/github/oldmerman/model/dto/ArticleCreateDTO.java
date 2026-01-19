package io.github.oldmerman.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArticleCreateDTO {

    private Long id;

    private String articleName;

    private String articleDecr;

    private Byte articleType;

    private List<String> attrs;
}
