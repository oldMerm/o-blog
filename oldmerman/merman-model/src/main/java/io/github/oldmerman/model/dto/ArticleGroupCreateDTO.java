package io.github.oldmerman.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArticleGroupCreateDTO {

    private Long groupId;

    private List<Long> articleIds;

}
