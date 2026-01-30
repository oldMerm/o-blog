package io.github.oldmerman.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserManageVO {

     private Long id;

     private String username;

     private Integer articleCount;

     private byte status;

     private LocalDateTime updatedAt;

     private String attr;
}
