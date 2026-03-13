package io.github.oldmerman.web.service;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.vo.FeedbackVO;

import java.util.List;

/**
 * 反馈功能相关接口
 * @author oldmerman
 * @date 2026-1-13
 */
public interface FeedbackService {

    /**
     * 获取反馈信息
     *
     * @return 封装feedback
     */
    FeedbackVO getFeedback();

    /**
     * 用户批量获取反馈信息
     *
     * @param userId 用户id
     * @return 反馈信息封装对象
     */
    List<FeedbackVO> getBatchFeedback(Long userId);

    /**
     * 分页查询反馈信息
     *
     * @param current 起始页
     * @param size 每页条数
     * @return 封装page对象
     */
    PageResult<FeedbackVO> page(Long current, Long size);

    /**
     * 创建反馈信息
     *
     * @param feedback 反馈信息
     */
    void createFeedback(String feedback, String feedbackType, Long userId);

    /**
     * 回复反馈信息
     *
     * @param id 用户id
     */
    void saveFeedback(Long id, String cont);

}
