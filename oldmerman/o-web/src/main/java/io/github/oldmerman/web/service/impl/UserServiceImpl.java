package io.github.oldmerman.web.service.impl;

import cn.hutool.core.util.ObjectUtil;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.common.util.HmacSHA256Util;
import io.github.oldmerman.common.util.IdGenerator;
import io.github.oldmerman.model.dto.UserManageDTO;
import io.github.oldmerman.model.po.FeedBack;
import io.github.oldmerman.model.po.User;
import io.github.oldmerman.model.vo.FeedbackVO;
import io.github.oldmerman.model.vo.UserInfoVO;
import io.github.oldmerman.web.converter.UserConverter;
import io.github.oldmerman.web.mapper.FeedbackMapper;
import io.github.oldmerman.web.mapper.UserMapper;
import io.github.oldmerman.web.service.OssService;
import io.github.oldmerman.web.service.UserService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final OssService ossService;

    private final UserMapper userMapper;

    private final FeedbackMapper feedbackMapper;

    private final UserConverter converter;

    /**
     * 获取用户信息接口
     * @param userId 用户id
     * @return 用户封装vo
     */
    public UserInfoVO getUsrInfo(Long userId) {
        User user = userMapper.selectSimUsrInfo(userId);
        if(user.getIsDelete() != 1){
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        UserInfoVO vo = converter.poToInfoVO(user);
        vo.setAttrURL(ossService.genPreviewUrl(user.getAttr()));
        return vo;
    }

    /**
     * 更新用户信息接口
     * @param dto 封装dto
     */
    public void updateUsrInfo(UserManageDTO dto) {
        if(ObjectUtil.isEmpty(dto)){
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        try {
            String oldPassword = dto.getPassword();
            if(oldPassword != null){
                String newPassword = HmacSHA256Util.hmacSha256(oldPassword);
                dto.setPassword(newPassword);
            }
        } catch (Exception e) {
            log.error("加密失败, {}",e.getMessage());
            throw new BusinessException(BusErrorCode.ENCRYPTION_FAILED);
        }
        userMapper.updateUserInfo(dto);
    }

    /**
     * 注销用户接口
     * @param userId 注销用户的id
     */
    public void deleteUsr(Long userId) {
        userMapper.logicDeleteUser(userId);
    }

    /**
     * 创建反馈信息
     * @param feedback 反馈信息
     */
    public void createFeedback(String feedback, Byte feedbackType, Long userId) {
        if(feedback.length() > 100){
            throw new BusinessException(BusErrorCode.LENGTH_EXCEEDS_LIMIT);
        }
        feedbackMapper.insert(FeedBack.builder()
                .feedbackType(feedbackType)
                .feedback(feedback)
                .userId(userId)
                .id(IdGenerator.nextId())
                .build());
    }

    /**
     * 获取反馈信息
     * @return 封装feedback
     */
    public FeedbackVO getFeedback() {
        return feedbackMapper.selectByUserId(UserContext.getUserId());
    }


}
