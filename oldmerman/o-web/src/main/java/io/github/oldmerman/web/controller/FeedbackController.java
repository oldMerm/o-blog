package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.FeedbackCreateDTO;
import io.github.oldmerman.model.dto.FeedbackSaveDTO;
import io.github.oldmerman.model.vo.FeedbackVO;
import io.github.oldmerman.web.service.FeedbackService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("feedback")
@Slf4j
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("info")
    public Result<FeedbackVO> getFeedback(){
        log.info("用户获取回馈信息");
        return Result.success(feedbackService.getFeedback());
    }

    @GetMapping("batch_info")
    public Result<List<FeedbackVO>> getBatchFeedback(){
        Long userId = UserContext.getUserId();
        log.info("用户:{},获取批量获取反馈信息",userId);
        return Result.success(feedbackService.getBatchFeedback(userId));
    }

    @GetMapping("page")
    public Result<PageResult<FeedbackVO>> page(@RequestParam(value = "current", defaultValue = "1") Long current,
                                               @RequestParam(value = "size", defaultValue = "7") Long size){
        log.info("分页查询反馈信息");
        return Result.success(feedbackService.page(current, size));
    }

    @PostMapping
    public Result<Void> createFeedback(@RequestBody FeedbackCreateDTO dto){
        Long userId = UserContext.getUserId();
        log.info("用户:{},反馈", userId);
        feedbackService.createFeedback(dto.getContent(), dto.getSelectIds(), userId);
        return Result.success();
    }

    @PostMapping("save")
    public Result<Void> saveFeedback(@RequestBody FeedbackSaveDTO dto){
        log.info("回复反馈信息:{}",dto.getId());
        feedbackService.saveFeedback(dto.getId(), dto.getCont());
        return Result.success();
    }


}
