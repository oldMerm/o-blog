package io.github.oldmerman.model.rpcp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRequest {

    private String sessionId;

    private String userId;

    private String message;

}
