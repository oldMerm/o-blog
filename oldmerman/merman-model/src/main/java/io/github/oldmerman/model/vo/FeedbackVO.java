package io.github.oldmerman.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackVO {

    private String id;

    private String username;

    private String replier;

    private String feedbackType;

    private String feedback;

    private String reply;

    private LocalDateTime repliedAt;
}
