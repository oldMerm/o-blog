package io.github.oldmerman.model.vo;

import lombok.Data;

@Data
public class AiConversationVO {

    private Long id;

    private String userId;

    private String sessionId;

    private String sessionDecr;

}
