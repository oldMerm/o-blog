package io.github.oldmerman.web.service;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.dto.UserToggleDTO;
import io.github.oldmerman.model.vo.UserManageVO;

public interface UserAdminService {

    PageResult<UserManageVO> page(Long current, Long size);

    String getUserPermission();

    void toggleUserStatus(UserToggleDTO dto);

}
