package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_article_like_record")
public class ArticleLikeRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long userId;

    private Long articleId;

    private LocalDateTime createdAt;
}
