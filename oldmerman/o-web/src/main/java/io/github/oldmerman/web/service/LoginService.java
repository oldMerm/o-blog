package io.github.oldmerman.web.service;

import io.github.oldmerman.model.dto.UserCreatedDTO;
import io.github.oldmerman.model.vo.CaptchaVO;

public interface LoginService {

    void register(UserCreatedDTO dto);

    CaptchaVO generateCaptcha();

}
