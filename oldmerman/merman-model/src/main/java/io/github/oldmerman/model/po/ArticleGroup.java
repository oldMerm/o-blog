package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_article_group")
public class ArticleGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String groupName;

    private String groupDesc;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
