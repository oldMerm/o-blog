package io.github.oldmerman.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ArticleTopVO {

    private List<ArticleRenderVO> newList;

    private List<ArticleRenderVO> tecList;

    private List<ArticleRenderVO> dailyList;
}
