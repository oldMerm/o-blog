package io.github.oldmerman.model.RPC;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRequest {

    private String sessionId;

    private Long userId;

    private String message;
}
