package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_article")
public class Article {

    @TableId
    private Long id;

    private Long writerId;

    private String key;

    private String articleName;

    private String articleWriter;

    private String articleDecr;

    private Byte articleType;

    private Byte articleStatus;

    private Integer like;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
