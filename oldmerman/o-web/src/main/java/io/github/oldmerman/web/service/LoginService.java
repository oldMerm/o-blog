package io.github.oldmerman.web.service;

import io.github.oldmerman.model.dto.LoginDTO;
import io.github.oldmerman.model.dto.UserCreatedDTO;
import io.github.oldmerman.model.vo.CaptchaVO;
import io.github.oldmerman.model.vo.LoginVO;

/**
 * 登录功能相关接口
 * @author oldmerman
 * @date 2025-12-25
 */
public interface LoginService {

    /**
     * 登录接口
     *
     * @param dto 封装dto
     * @return 访问令牌，刷新令牌，过期时间
     */
    LoginVO login(LoginDTO dto);

    /**
     * 登出接口
     *
     * @param sign 标识
     */
    void logout(String sign);

    /**
     * 用户注册，邮箱验证
     *
     * @param dto 封装dto
     */
    void register(UserCreatedDTO dto);

    /**
     * 用户注销
     *
     * @param sign 唯一标识
     */
    void logoff(String sign);

    /**
     * 刷新token，传入旧token签发新token
     *
     * @param sign 过期的accessToken
     * @return 访问令牌，刷新令牌，过期时间
     */
    LoginVO refreshToken(String sign);

    /**
     * 获取验证码
     * 生成对应的验证码，并存入redis，十分钟过期
     *
     * @return base64验证码，验证key(uuid
     */
    CaptchaVO generateCaptcha();

    /**
     * 发送验证码到邮箱
     *
     * @param email 邮箱
     */
    void sendEmail(String email);

    /**
     * 判断是否为合法管理用户
     *
     * @param userId 用户id
     */
    void isValidAuthToken(Long userId);
}
