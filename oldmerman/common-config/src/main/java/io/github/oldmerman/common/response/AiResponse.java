package io.github.oldmerman.common.response;

import lombok.Data;

@Data
public class AiResponse {

    public int code;

    public String data;

    public String message;

}
