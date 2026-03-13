package io.github.oldmerman.web.service;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.dto.UserToggleDTO;
import io.github.oldmerman.model.vo.UserManageVO;

/**
 * 管理端用户功能相关接口
 * @author oldmerman
 * @date 2026-3-10
 */
public interface UserAdminService {

    /**
     * 分页查询用户管理信息，用户管理端
     *
     * @param current 起始页面
     * @param size    页面大小
     * @return 分页list
     */
    PageResult<UserManageVO> page(Long current, Long size);

    /**
     * 获取用户权限
     * @return 用户权限
     */
    String getUserPermission();

    /**
     * 更新用户状态
     *
     * @param dto 封装dto
     */
    void toggleUserStatus(UserToggleDTO dto);

}
