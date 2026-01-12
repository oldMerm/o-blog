package io.github.oldmerman.web.service;

import io.github.oldmerman.model.vo.FeedbackVO;

import java.util.List;

public interface FeedbackService {

    void createFeedback(String feedback, String feedbackType, Long userId);

    FeedbackVO getFeedback();
}
