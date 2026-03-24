package io.github.oldmerman.model.vo;

import lombok.Data;

@Data
public class AiMessagesVO {

    private String sessionId;

    private String role;

    private String content;

    private int tokens;

}
