package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("o_feedback")
@Builder
public class FeedBack {

    @TableId
    private Long id;

    private Long userId;

    private String feedbackType;

    private String feedback;

    private String reply;

    private LocalDateTime createdAt;
}
