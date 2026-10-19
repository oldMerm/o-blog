package io.github.oldmerman.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ArticlePageDetailVO {

    private String id;

    private String articleName;

    private String articleDecr;

    private List<String> groupNameList;

}
