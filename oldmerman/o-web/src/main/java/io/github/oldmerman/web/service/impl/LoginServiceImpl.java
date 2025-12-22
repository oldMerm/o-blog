package io.github.oldmerman.web.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import io.github.oldmerman.model.dto.UserCreatedDTO;
import io.github.oldmerman.model.vo.CaptchaVO;
import io.github.oldmerman.web.service.LoginService;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Override
    public void register(UserCreatedDTO dto) {

    }

    @Override
    public CaptchaVO generateCaptcha() {
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(200, 100);

        return null;
    }
}
