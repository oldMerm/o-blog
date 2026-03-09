package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.UserToggleDTO;
import io.github.oldmerman.model.vo.UserManageVO;
import io.github.oldmerman.web.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/usr")
@Slf4j
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userService;

    @GetMapping("page")
    public Result<PageResult<UserManageVO>> page(@RequestParam(name = "current", defaultValue = "1") Long current,
                                                 @RequestParam(name = "size", defaultValue = "7") Long size){
        return Result.success(userService.page(current, size));
    }

    @GetMapping("isAdmin")
    public Result<String> getUserPermission(){
        return Result.success(userService.getUserPermission());
    }

    @PostMapping("toggle")
    public Result<Void> toggleUserStatus(@RequestBody UserToggleDTO dto){
        log.info("修改用户状态,{}",dto.getId());
        userService.toggleUserStatus(dto);
        return Result.success();
    }

}
