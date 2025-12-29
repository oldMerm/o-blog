package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.vo.OssPutVO;
import io.github.oldmerman.web.service.OssService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("oss")
@Slf4j
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    @PutMapping("upload")
    public Result<String> uploadUsrImage(@RequestParam("file") MultipartFile file){
        Long userId = UserContext.getUserId();
        log.info("上传图片，{}",userId);
        return Result.success(ossService.uploadUsrImage(userId, file));
    }
}
