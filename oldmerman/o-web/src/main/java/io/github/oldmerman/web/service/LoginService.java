package io.github.oldmerman.web.service;

import io.github.oldmerman.model.dto.LoginDTO;
import io.github.oldmerman.model.dto.UserCreatedDTO;
import io.github.oldmerman.model.vo.CaptchaVO;
import io.github.oldmerman.model.vo.LoginVO;

public interface LoginService {

    LoginVO login(LoginDTO dto);

    void logout(String sign);

    void register(UserCreatedDTO dto);

    void logoff(String sign);

    LoginVO refreshToken(String sign);

    CaptchaVO generateCaptcha();

    void sendEmail(String email);
}
