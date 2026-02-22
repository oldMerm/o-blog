package io.github.oldmerman.web.service;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.vo.FeedbackVO;

import java.util.List;

public interface FeedbackService {

    FeedbackVO getFeedback();

    List<FeedbackVO> getBatchFeedback(Long userId);

    PageResult<FeedbackVO> page(Long current, Long size);

    void createFeedback(String feedback, String feedbackType, Long userId);

    void saveFeedback(Long id, String cont);

}
