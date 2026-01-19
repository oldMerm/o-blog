package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_article")
@Builder
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

    public static class ArticleType {
        public static byte TECHNOLOGY = 1;
        public static byte DAILY = 2;
    }

    public static class ArticleStatus {
        public static byte UNDER_REVIEW = 1;
        public static byte REVIEWED = 2;
        public static byte PUBLISHED = 3;
        public static byte NOT_REVIEWED = 4;
    }
}
