package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.UserCreatedDTO;
import io.github.oldmerman.model.vo.CaptchaVO;
import io.github.oldmerman.web.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("register")
    public Result<Void> register(@RequestBody UserCreatedDTO dto){
        log.info("注册用户：{}", dto.getUsername());
        loginService.register(dto);
        return Result.success();
    }

    @GetMapping("captcha")
    public Result<CaptchaVO> generateCaptcha(){
        log.info("获取验证码");
        return Result.success(loginService.generateCaptcha());
    }

}
