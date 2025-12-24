package io.github.oldmerman.web.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.ObjectUtil;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.enums.WebEnum;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.common.util.IdGenerator;
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
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {
    private final LoginMapper loginMapper;

    private final StringRedisTemplate redisTemplate;

    private final EmailSender sender;

    private final LoginConverter converter;

    public LoginVO login(LoginDTO dto) {
        return null;
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
        po.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        loginMapper.createUser(po);
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
     * 检验邮箱，用户名，密码
     * @param dto 封装对象
     */
    private void checkCreateDTO(UserCreatedDTO dto){
        if(!RegexUtils.isValidEmail(dto.getEmail())){
            throw new BusinessException(BusErrorCode.EMAIL_WRONG_FORMAT);
        }else if(!RegexUtils.isValidPassword(dto.getPassword())){
            throw new BusinessException(BusErrorCode.PASSWORD_WRONG_FORMAT);
        }else if(!RegexUtils.isValidUsername(dto.getUsername())){
            throw new BusinessException(BusErrorCode.USERNAME_WRONG_FORMAT);
        }
    }
}
