package io.github.oldmerman.web.service;

import io.github.oldmerman.common.response.Result;

/**
 * 文章点赞记录相关接口
 * @author oldmerman
 * @date 2026-8-31
 */
public interface ArticleLikeRecordService {

    /**
     * 获取用户点赞数据
     * @param userId 用户唯一标识
     * @param articleId 文章唯一标识
     * @return true -> 已点赞，反之
     */
    Boolean getLikeRecord(Long userId, Long articleId);

    /**
     * 文章点赞，只能点击一次
     * @param userId 用户唯一标识
     * @param articleId 文章唯一标识
     */
    Boolean saveLikeRecord(Long userId, Long articleId);
}
