package io.github.oldmerman.web.service;

import io.github.oldmerman.model.dto.UserManageDTO;
import io.github.oldmerman.model.vo.FeedbackVO;
import io.github.oldmerman.model.vo.UserInfoVO;

public interface UserService {

    UserInfoVO getUsrInfo(Long userId);

    void updateUsrInfo(UserManageDTO dto);

    void deleteUsr(Long userId);

    void createFeedback(String feedback, Byte feedbackType, Long userId);

    FeedbackVO getFeedback();

}
