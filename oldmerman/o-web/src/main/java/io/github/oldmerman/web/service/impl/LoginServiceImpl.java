package io.github.oldmerman.web.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.enums.WebEnum;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.util.RegexUtils;
import io.github.oldmerman.model.dto.LoginDTO;
import io.github.oldmerman.model.dto.UserCreatedDTO;
import io.github.oldmerman.model.vo.CaptchaVO;
import io.github.oldmerman.model.vo.LoginVO;
import io.github.oldmerman.web.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final StringRedisTemplate redisTemplate;

    public LoginVO login(LoginDTO dto) {
        return null;
    }

    public void register(UserCreatedDTO dto) {

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
        if(RegexUtils.isValidEmail(email)){
            throw new BusinessException(BusErrorCode.WRONG_EMAIL_FORMAT);
        }


    }
}
