package io.github.oldmerman.web.service;

import io.github.oldmerman.model.po.AiConversation;

import java.util.List;

/**
 * ai功能相关业务
 * @author oldmerman
 * @date 2026-3-23
 */
public interface AiService {

    /**
     * 用户获取会话信息
     *
     * @return 会话信息对应的集合
     */
    List<AiConversation> getSessions();

    /**
     * 用户创建会话
     */
    void createSession();
}
