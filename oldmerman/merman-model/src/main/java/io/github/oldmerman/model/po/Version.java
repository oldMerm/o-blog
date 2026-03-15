package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_version")
public class Version {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String objectName;

    private String versionContent;

    private String versionId;

    private LocalDateTime createdAt;
}
