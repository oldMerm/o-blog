package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.UserManageDTO;
import io.github.oldmerman.model.vo.UserInfoVO;
import io.github.oldmerman.web.service.UserService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("usr")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("info")
    public Result<UserInfoVO> getUsrInfo(){
        Long userId = UserContext.getUserId();
        log.info("获取用户信息：{}",userId);
        return Result.success(userService.getUsrInfo(userId));
    }

    @PostMapping("manage")
    public Result<Void> updateUsrInfo(@RequestBody UserManageDTO dto){
        dto.setId(UserContext.getUserId());
        log.info("更新用户数据：{}",dto.getId());
        userService.updateUsrInfo(dto);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> deleteUsr(){
        Long userId = UserContext.getUserId();
        log.info("用户账户注销:{}",userId);
        userService.deleteUsr(userId);
        return Result.success();
    }
}
