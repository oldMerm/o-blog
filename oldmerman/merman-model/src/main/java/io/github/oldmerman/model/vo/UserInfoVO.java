package io.github.oldmerman.model.vo;

import lombok.Data;

@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private int article;

    private int like;

    private String attrURL;

    private String updatedAt;

}
