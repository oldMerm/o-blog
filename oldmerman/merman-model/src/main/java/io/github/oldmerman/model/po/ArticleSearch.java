package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_article_search")
public class ArticleSearch {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long articleId;

    private String title;

    private String summary;

    private LocalDateTime createdAt;
}
