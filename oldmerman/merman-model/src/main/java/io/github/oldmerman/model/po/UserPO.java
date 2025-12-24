package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_user")
public class UserPO {

    @TableId
    private Long id;

    private String username;

    private String email;

    private String password;

    private int article;

    private int like;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private byte isDelete;
}
