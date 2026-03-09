package io.github.oldmerman.web.service;

import io.github.oldmerman.model.dto.UserManageDTO;
import io.github.oldmerman.model.po.Counter;
import io.github.oldmerman.model.vo.UserInfoVO;

import java.util.List;

public interface UserService {

    UserInfoVO getUsrInfo(Long userId);

    List<Counter> getUserMonCount(Long count);

    void updateUsrInfo(UserManageDTO dto);

    void deleteUsr(Long userId, String token);

}
