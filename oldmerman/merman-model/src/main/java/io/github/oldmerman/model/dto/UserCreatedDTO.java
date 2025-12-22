package io.github.oldmerman.model.dto;

import lombok.Data;

@Data
public class UserCreatedDTO {

    private String username;

    private String email;

    private String password;
}
