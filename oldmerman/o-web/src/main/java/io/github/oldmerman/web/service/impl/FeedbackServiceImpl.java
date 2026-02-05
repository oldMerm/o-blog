package io.github.oldmerman.web.service.impl;

import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.util.IdGenerator;
import io.github.oldmerman.model.po.FeedBack;
import io.github.oldmerman.model.vo.FeedbackVO;
import io.github.oldmerman.web.converter.FeedbackConverter;
import io.github.oldmerman.web.mapper.FeedbackMapper;
import io.github.oldmerman.web.service.FeedbackService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    private final FeedbackConverter converter;

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
     * 用户批量获取反馈信息
     * @param userId 用户id
     * @return 反馈信息封装对象
     */
    public List<FeedbackVO> getBatchFeedback(Long userId) {
        List<FeedBack> list = feedbackMapper.selectBatchByUserId(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return list.stream()
                .map(i -> {
                    FeedbackVO feedbackVO = converter.poToVo(i);
                    if (i.getRepliedAt() != null) {
                        feedbackVO.setRepliedAt(formatter.format(i.getRepliedAt()));
                    }
                    return feedbackVO;
                }).toList();
    }

    /**
     * 获取反馈信息
     * @return 封装feedback
     */
    public FeedbackVO getFeedback() {
        return feedbackMapper.selectByUserId(UserContext.getUserId());
    }
}
