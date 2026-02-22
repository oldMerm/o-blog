package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.common.util.IdGenerator;
import io.github.oldmerman.model.po.Feedback;
import io.github.oldmerman.model.vo.FeedbackVO;
import io.github.oldmerman.web.converter.FeedbackConverter;
import io.github.oldmerman.web.mapper.FeedbackMapper;
import io.github.oldmerman.web.mapper.UserMapper;
import io.github.oldmerman.web.service.FeedbackService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    private final UserMapper userMapper;

    private final FeedbackConverter converter;

    /**
     * 获取反馈信息
     *
     * @return 封装feedback
     */
    public FeedbackVO getFeedback() {
        return feedbackMapper.selectByUserId(UserContext.getUserId());
    }

    /**
     * 用户批量获取反馈信息
     *
     * @param userId 用户id
     * @return 反馈信息封装对象
     */
    public List<FeedbackVO> getBatchFeedback(Long userId) {
        List<Feedback> list = feedbackMapper.selectBatchByUserId(userId);
        return list.stream()
                .map(i -> {
                    FeedbackVO feedbackVO = converter.poToVo(i);
                    if (i.getRepliedAt() != null) {
                        feedbackVO.setRepliedAt(i.getRepliedAt());
                    }
                    return feedbackVO;
                }).toList();
    }

    /**
     * 分页查询反馈信息
     *
     * @param current 起始页
     * @param size 每页条数
     * @return 封装page对象
     */
    public PageResult<FeedbackVO> page(Long current, Long size) {
        Page<Feedback> page = new Page<>(current, size);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Feedback::getCreatedAt);
        IPage<Feedback> iPage = feedbackMapper.selectPage(page, wrapper);
        List<FeedbackVO> voList = iPage.getRecords().stream()
                .map(i -> {
                    FeedbackVO feedbackVO = converter.poToVo(i);
                    feedbackVO.setUsername(userMapper.selectNameById(i.getUserId()));
                    return feedbackVO;
                })
                .toList();
        return PageResult.of(
                iPage.getCurrent(),
                iPage.getSize(),
                iPage.getTotal(),
                voList
        );
    }

    /**
     * 创建反馈信息
     *
     * @param feedback 反馈信息
     */
    public void createFeedback(String feedback, String feedbackType, Long userId) {
        if(feedback.length() > 100){
            throw new BusinessException(BusErrorCode.LENGTH_EXCEEDS_LIMIT);
        }
        feedbackMapper.insert(Feedback.builder()
                .feedbackType(feedbackType)
                .feedback(feedback)
                .userId(userId)
                .id(IdGenerator.nextId())
                .build());
    }

    /**
     * 回复反馈信息
     *
     * @param id 用户id
     */
    public void saveFeedback(Long id, String cont) {
        LambdaUpdateWrapper<Feedback> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Feedback::getId, id)
                .set(Feedback::getReply, cont)
                .set(Feedback::getReplier, userMapper.selectUserById(UserContext.getUserId()).getUsername())
                .set(Feedback::getRepliedAt, LocalDateTime.now());
        feedbackMapper.update(wrapper);
    }
}
