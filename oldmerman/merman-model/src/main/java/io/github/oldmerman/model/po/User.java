package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o-user")
public class User {

    @TableId
    private Long id;

    private String username;

    private String email;

    private String password;

    private Integer article;

    private Integer like;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private byte isDelete;
}
