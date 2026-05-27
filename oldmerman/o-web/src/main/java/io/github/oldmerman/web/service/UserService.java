package io.github.oldmerman.web.service;

import io.github.oldmerman.model.dto.UserManageDTO;
import io.github.oldmerman.model.po.Counter;
import io.github.oldmerman.model.vo.UserInfoVO;

import java.util.List;

/**
 * 用户功能相关接口
 * @author oldmerman
 * @date 2026-1-15
 */
public interface UserService {

    /**
     * 获取用户信息接口
     *
     * @param userId 用户id
     * @return 用户封装vo
     */
    UserInfoVO getUsrInfo(Long userId);

    /**
     * 获取用户月数据
     *
     * @return List<Counter>
     */
    List<Counter> getUserMonCount(Long count);

    /**
     * 更新用户信息接口
     *
     * @param dto 封装dto
     */
    void updateUsrInfo(UserManageDTO dto);

    /**
     * 更新用户文章数量接口
     *
     * @param userId 用户id
     * @param number 数目
     * @param isAdd true -> 新增，反之
     */
    void updateUsrArticle(Long userId, Integer number, Boolean isAdd);

    /**
     * 注销用户接口
     *
     * @param userId 注销用户的id
     */
    void deleteUsr(Long userId, String token);

}
