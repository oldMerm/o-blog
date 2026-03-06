package io.github.oldmerman.web.service;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.dto.UserManageDTO;
import io.github.oldmerman.model.dto.UserToggleDTO;
import io.github.oldmerman.model.po.Counter;
import io.github.oldmerman.model.vo.UserInfoVO;
import io.github.oldmerman.model.vo.UserManageVO;

import java.util.List;

public interface UserService {

    UserInfoVO getUsrInfo(Long userId);

    void updateUsrInfo(UserManageDTO dto);

    List<Counter> getUserMonCount(Long count);

    String getUserPermission();

    void deleteUsr(Long userId, String token);

    PageResult<UserManageVO> page(Long current, Long size);

    void toggleUserStatus(UserToggleDTO dto);
}
