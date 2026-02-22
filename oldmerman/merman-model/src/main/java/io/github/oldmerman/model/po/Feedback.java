package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_feedback")
@Builder
public class Feedback {

    @TableId
    private Long id;

    private Long userId;

    private String replier;

    private String feedbackType;

    private String feedback;

    private String reply;

    private LocalDateTime createdAt;

    private LocalDateTime repliedAt;
}
