package io.github.oldmerman.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {

    private String token;

    private String refreshToken;

    private Long timeout; // minutes
}
