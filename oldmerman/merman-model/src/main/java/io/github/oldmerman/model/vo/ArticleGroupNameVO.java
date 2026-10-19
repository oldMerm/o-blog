package io.github.oldmerman.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;


@Data
public class ArticleGroupNameVO {

    @TableId(type = IdType.AUTO)
    private Long articleId;

    private Long groupId;

    private String groupName;

}
