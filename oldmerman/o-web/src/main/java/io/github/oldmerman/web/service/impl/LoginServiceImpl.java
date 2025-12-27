package io.github.oldmerman.web.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.RandomUtil;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.enums.NumEnum;
import io.github.oldmerman.common.enums.WebEnum;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.common.util.HmacSHA256Util;
import io.github.oldmerman.common.util.IdGenerator;
import io.github.oldmerman.common.util.JwtUtil;
import io.github.oldmerman.common.util.RegexUtils;
import io.github.oldmerman.model.dto.LoginDTO;
import io.github.oldmerman.model.dto.UserCreatedDTO;
import io.github.oldmerman.model.po.UserPO;
import io.github.oldmerman.model.vo.CaptchaVO;
import io.github.oldmerman.model.vo.LoginVO;
import io.github.oldmerman.web.converter.LoginConverter;
import io.github.oldmerman.web.mapper.LoginMapper;
import io.github.oldmerman.web.service.LoginService;
import io.github.oldmerman.web.util.EmailSender;
import io.github.oldmerman.web.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final LoginMapper loginMapper;

    private final StringRedisTemplate redisTemplate;

    private final JwtUtil jwtUtil;

    private final EmailSender sender;

    private final LoginConverter converter;

    /**
     * 登录接口
     * @param dto 封装dto
     * @return 访问令牌，刷新令牌，过期时间
     */
    public LoginVO login(LoginDTO dto) {
        if(!dto.getCode().equals(redisTemplate.opsForValue().get(dto.getUuid()))){
            log.info(redisTemplate.opsForValue().get(dto.getUuid()));
            throw new BusinessException(BusErrorCode.ERROR_VERIFY_CODE);
        }
        // 校验密码并获取凭证
        String password;
        try {
            password = HmacSHA256Util.hmacSha256(dto.getPassword());
        } catch (Exception e) {
            log.error("{},加密失败",dto.getUsername());
            throw new BusinessException(BusErrorCode.ENCRYPTION_FAILED);
        }
        Long id;
        String username = dto.getUsername();
        if(RegexUtils.isValidEmail(username)){
            id = loginMapper.verifyUserInfoByEmail(username, password);
        }else{
            id = loginMapper.verifyUserInfoById(username, password);
        }
        if(id == null){
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        // 签发令牌
        String sign = String.valueOf(id);
        String accessToken = jwtUtil.generateAccessToken(sign, null);
        String refreshToken = jwtUtil.generateRefreshToken(sign);
        redisTemplate.opsForValue().set(RedisPrefix.REFRESH_TOKEN + sign, refreshToken,
                NumEnum.REFRESH_TOKEN_EXPIRATION.getValue(), TimeUnit.MINUTES);
        return LoginVO.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .timeout(NumEnum.ACCESS_TOKEN_EXPIRATION.getValue())
                .build();
    }

    /**
     * 登出接口
     * @param sign 标识
     */
    public void logout(String sign) {
        if(sign == null){
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        String token = sign.substring(WebEnum.AUTH_PREFIX.getValue().length());
        Claims claims = jwtUtil.parseToken(token);
        String refreshSign = claims.getSubject();
        redisTemplate.opsForValue().set(RedisPrefix.BLACK_TOKEN + token, "1",
                claims.getExpiration().getTime() + 1000*60, TimeUnit.MILLISECONDS);
        redisTemplate.delete(RedisPrefix.REFRESH_TOKEN + refreshSign);
    }

    /**
     * 用户注册，邮箱验证
     * @param dto 封装dto
     */
    public void register(UserCreatedDTO dto) {
        checkCreateDTO(dto);
        String code = redisTemplate.opsForValue().get(RedisPrefix.EMAIL_CHECK + dto.getEmail());
        if(!code.equals(dto.getCode())){
            throw new BusinessException(BusErrorCode.ERROR_EMAIL_CODE);
        }
        UserPO po = converter.createToUserPO(dto);
        po.setId(IdGenerator.nextId());
        po.setUsername(randomUsername());
        try {
            po.setPassword(HmacSHA256Util.hmacSha256(dto.getPassword()));
        } catch (Exception e) {
            log.error("{},加密失败",dto.getUsername());
            throw new BusinessException(BusErrorCode.ENCRYPTION_FAILED);
        }
        loginMapper.createUser(po);
    }

    /**
     * 用户注销
     * @param sign 唯一标识
     */
    public void logoff(String sign) {
        logout(sign);
        Long userId = UserContext.getUserId();
        loginMapper.logoffByUserId(userId);
    }

    /**
     * 刷新token，传入旧token签发新token
     * @param sign 过期的accessToken
     * @return 访问令牌，刷新令牌，过期时间
     */
    public LoginVO refreshToken(String sign) {
        Claims claims = jwtUtil.parseToken(sign);
        String userId = claims.getSubject();
        String refreshToken = redisTemplate.opsForValue().get(RedisPrefix.REFRESH_TOKEN + userId);
        if(jwtUtil.isTokenExpiring(refreshToken)){
            throw new BusinessException(BusErrorCode.TOKEN_EXPIRED);
        }
        String accessToken = jwtUtil.generateAccessToken(userId, null);
        return LoginVO.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .timeout(NumEnum.ACCESS_TOKEN_EXPIRATION.getValue())
                .build();
    }

    /**
     * 获取验证码
     * 生成对应的验证码，并存入redis，十分钟过期
     * @return base64验证码，验证key(uuid
     */
    public CaptchaVO generateCaptcha() {
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(200, 100);
        String uuid = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(uuid, circleCaptcha.getCode(), 10, TimeUnit.MINUTES);
        CaptchaVO vo = new CaptchaVO();
        vo.setCaptcha(WebEnum.BASE64_IMAGE_PREFIX.getValue() + circleCaptcha.getImageBase64());
        vo.setUuid(uuid);
        return vo;
    }

    /**
     * 发送验证码到邮箱
     * @param email 邮箱
     */
    public void sendEmail(String email) {
        if(!RegexUtils.isValidEmail(email)){
            throw new BusinessException(BusErrorCode.EMAIL_WRONG_FORMAT);
        }
        try {
            redisTemplate.opsForValue().set(RedisPrefix.EMAIL_CHECK + email,
                    sender.sendCheckEmail(email),
                    10, TimeUnit.MINUTES);
        } catch (MessagingException e) {
            log.error("邮件发送失败,{}",e.getMessage());
            throw new BusinessException(BusErrorCode.EMAIL_SEND_FAILED);
        }
    }

    /**
     * 检验邮箱，密码
     * @param dto 封装对象
     */
    private void checkCreateDTO(UserCreatedDTO dto){
        if(!RegexUtils.isValidEmail(dto.getEmail())){
            throw new BusinessException(BusErrorCode.EMAIL_WRONG_FORMAT);
        }else if(!RegexUtils.isValidPassword(dto.getPassword())){
            throw new BusinessException(BusErrorCode.PASSWORD_WRONG_FORMAT);
        }
    }

    /**
     * 生成一个随机的用户名，格式usr-laoyu1
     * @return 初始用户名
     */
    private String randomUsername(){
        return WebEnum.USER_PREFIX.getValue() + "-" + RandomUtil.randomString(6);
    }
}
