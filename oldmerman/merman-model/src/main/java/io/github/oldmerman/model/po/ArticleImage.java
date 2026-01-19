package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_article_img")
public class ArticleImage {

    @TableId
    private Long id;

    private Long articleId;

    private String url;

    private LocalDateTime createdAt;
}
