package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.web.service.OssService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("oss")
@Slf4j
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    @PutMapping("upload")
    public Result<String> uploadUsrImage(@RequestParam("img") MultipartFile file){
        Long userId = UserContext.getUserId();
        log.info("上传图片，{}",userId);
        return Result.success(ossService.uploadUsrImage(userId, file));
    }

    @PutMapping("img/uploadBatch")
    public Result<List<String>> uploadBatch(@RequestParam("id") Long id,
                                            @RequestParam("img") List<MultipartFile> files){
        Long userId = UserContext.getUserId();
        log.info("用户：{}，批量上传图片(其它)",userId);
        return Result.success(ossService.uploadBatch(id, files));
    }
}
