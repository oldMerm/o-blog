package io.github.oldmerman.web.service;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.dto.UserManageDTO;
import io.github.oldmerman.model.vo.UserInfoVO;
import io.github.oldmerman.model.vo.UserManageVO;

public interface UserService {

    UserInfoVO getUsrInfo(Long userId);

    void updateUsrInfo(UserManageDTO dto);

    void deleteUsr(Long userId, String token);

    PageResult<UserManageVO> page(Long current, Long size);

}
