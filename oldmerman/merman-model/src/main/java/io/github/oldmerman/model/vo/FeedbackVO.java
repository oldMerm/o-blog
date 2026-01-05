package io.github.oldmerman.model.vo;

import lombok.Data;

@Data
public class FeedbackVO {

    private Long id;

    private Byte feedbackType;

    private String feedback;

    private String reply;

}
