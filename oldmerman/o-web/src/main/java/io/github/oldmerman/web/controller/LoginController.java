package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.LoginDTO;
import io.github.oldmerman.model.dto.UserCreatedDTO;
import io.github.oldmerman.model.vo.CaptchaVO;
import io.github.oldmerman.model.vo.LoginVO;
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

    @PostMapping("login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto){
        log.info("用户登录,{}",dto.getUsername());
        return Result.success(loginService.login(dto));
    }

    @PostMapping("logout")
    public Result<Void> logout(@RequestHeader("Authorization") String sign){
        log.info("用户登出,{}",sign);
        loginService.logout(sign);
        return Result.success();
    }

    @PostMapping("register")
    public Result<Void> register(@RequestBody UserCreatedDTO dto){
        log.info("注册用户：{}", dto.getUsername());
        loginService.register(dto);
        return Result.success();
    }

    @DeleteMapping("logoff")
    public Result<Void> logoff(@RequestHeader("Authorization") String sign){
        log.info("用户注销:{}",sign);
        loginService.logoff(sign);
        return Result.success();
    }

    @GetMapping("refresh")
    public Result<LoginVO> refreshToken(@RequestHeader("Authorization") String sign){
        log.info("刷新Token,{}",sign);
        return Result.success(loginService.refreshToken(sign));
    }

    @GetMapping("captcha")
    public Result<CaptchaVO> generateCaptcha(){
        log.info("获取验证码");
        return Result.success(loginService.generateCaptcha());
    }

    @PostMapping("email")
    public Result<Void> sendEmail(@RequestParam("email") String email){
        log.info("发送验证码到，email: {}",email);
        loginService.sendEmail(email);
        return Result.success();
    }

}
