package io.github.oldmerman.model.dto;

import lombok.Data;

@Data
public class AiMessagesDTO {

    private String role;

    private String sessionId;

    private String content;
}
