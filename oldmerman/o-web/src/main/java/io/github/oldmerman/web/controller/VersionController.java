package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.VersionDTO;
import io.github.oldmerman.model.po.Version;
import io.github.oldmerman.web.service.VersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/version")
@Slf4j
@RequiredArgsConstructor
public class VersionController {

    private final VersionService service;

    @GetMapping("/page")
    public Result<PageResult<Version>> page(@RequestParam(name = "current", defaultValue = "1") Long current,
                                            @RequestParam(name = "size", defaultValue = "10") Long size){
        return Result.success(service.page(current, size));
    }

    @GetMapping
    public Result<Version> getVersionInfo(){
        return Result.success(service.getVersionInfo());
    }

    @PostMapping("/admin/save")
    public Result<Void> saveVersionInfo(@RequestBody VersionDTO dto){
        log.info("版本更新:{}",dto.getVersionId());
        service.saveVersionInfo(dto);
        return Result.success();
    }

    @DeleteMapping("/admin/delete/{id}")
    public Result<Void> deleteVersionInfo(@PathVariable("id") Long id){
        log.info("删除版本信息,{}",id);
        service.deleteVersionInfo(id);
        return Result.success();
    }
}
