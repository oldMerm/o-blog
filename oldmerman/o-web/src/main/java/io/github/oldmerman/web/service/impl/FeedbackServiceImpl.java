package io.github.oldmerman.web.service.impl;

import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.util.IdGenerator;
import io.github.oldmerman.model.po.FeedBack;
import io.github.oldmerman.model.vo.FeedbackVO;
import io.github.oldmerman.web.mapper.FeedbackMapper;
import io.github.oldmerman.web.service.FeedbackService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    /**
     * 创建反馈信息
     * @param feedback 反馈信息
     */
    public void createFeedback(String feedback, String feedbackType, Long userId) {
        if(feedback.length() > 100){
            throw new BusinessException(BusErrorCode.LENGTH_EXCEEDS_LIMIT);
        }
        feedbackMapper.insert(FeedBack.builder()
                .feedbackType(feedbackType)
                .feedback(feedback)
                .userId(userId)
                .id(IdGenerator.nextId())
                .build());
    }

    /**
     * 获取反馈信息
     * @return 封装feedback
     */
    public FeedbackVO getFeedback() {
        return feedbackMapper.selectByUserId(UserContext.getUserId());
    }
}
