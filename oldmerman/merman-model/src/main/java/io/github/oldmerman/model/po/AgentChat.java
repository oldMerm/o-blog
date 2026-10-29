package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_agent_chat")
public class AgentChat {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String threadId;
    private Long userId;
    private String state;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
