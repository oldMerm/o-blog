package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.UserManageDTO;
import io.github.oldmerman.model.vo.UserInfoVO;
import io.github.oldmerman.model.vo.UserManageVO;
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

    @GetMapping("page")
    public Result<PageResult<UserManageVO>> page(@RequestParam(name = "current", defaultValue = "1") Long current,
                                                       @RequestParam(name = "size", defaultValue = "7") Long size){
        log.info("分页查询用户数据: {}",current);
        return Result.success(userService.page(current, size));
    }

    @PostMapping("manage")
    public Result<Void> updateUsrInfo(@RequestBody UserManageDTO dto){
        dto.setId(UserContext.getUserId());
        log.info("更新用户数据：{}",dto.getId());
        userService.updateUsrInfo(dto);
        return Result.success();
    }

    @DeleteMapping("delete")
    public Result<Void> deleteUsr(@RequestHeader("Authorization") String token){
        Long userId = UserContext.getUserId();
        log.info("用户账户注销:{}",userId);
        userService.deleteUsr(userId, token);
        return Result.success();
    }


}
