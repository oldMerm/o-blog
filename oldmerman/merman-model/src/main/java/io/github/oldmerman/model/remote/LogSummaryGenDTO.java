package io.github.oldmerman.model.remote;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

@Data
public class LogSummaryGenDTO {

    @Alias("call_id")
    private String callId;

    private String text;
}
