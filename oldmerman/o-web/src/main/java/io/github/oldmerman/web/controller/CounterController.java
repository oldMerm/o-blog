package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.po.Counter;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import io.github.oldmerman.web.service.CounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("counter")
@Slf4j
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;

    @GetMapping("time")
    public Result<Integer> getSystemTime(){
        return Result.success(counterService.getSystemTime());
    }

    @GetMapping("incr/{type}")
    public Result<List<Counter>> getUserIncr(@PathVariable Long type){
        log.info("获取用户增长量");
        return Result.success(counterService.getIncr(type));
    }

    @GetMapping("newArt")
    public Result<ArticleRenderVO> getArticleUpdateInfo(){
        log.info("获取老鱼人最新文章");
        return Result.success(counterService.getArticleUpdateInfo());
    }

    @GetMapping("health")
    public Result<String> health(){
        return Result.success("I am healthy");
    }
}
