package io.github.oldmerman.model.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private String refreshToken;

    private Long timeout; // minutes
}
