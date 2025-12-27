package io.github.oldmerman.web.service;

import io.github.oldmerman.model.vo.UserInfoVO;

public interface UserService {

    UserInfoVO getUsrInfo(Long userId);
}
