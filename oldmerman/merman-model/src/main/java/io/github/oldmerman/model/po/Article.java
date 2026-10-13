package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("o_article")
@Builder
public class Article implements Serializable {

    @TableId(type = IdType.INPUT)
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
        public static final byte NEWS = 0;
        public static final byte TECHNOLOGY = 1;
        public static final byte DAILY = 2;
    }

    public static class ArticleStatus {
        public static byte UNDER_REVIEW = 1;
        public static byte REVIEWED = 2;
        public static byte PUBLISHED = 3;
        public static byte REMOVED = 4;
    }

    public String articleType() {
        if (articleType == null) {
            return "未知";
        }
        if (articleType == ArticleType.NEWS) {
            return "公告";
        } else if (articleType == ArticleType.TECHNOLOGY) {
            return "技术";
        } else if (articleType == ArticleType.DAILY) {
            return "日常";
        }
        return "未知";
    }
}
