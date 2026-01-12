package io.github.oldmerman.web.controller;

import io.github.oldmerman.common.response.Result;
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

    @PostMapping
    public Result<Void> createFeedback(@RequestParam String feedback,
                                       @RequestParam String feedbackType){
        Long userId = UserContext.getUserId();
        log.info("用户:{},反馈", userId);
        feedbackService.createFeedback(feedback, feedbackType, userId);
        return Result.success();
    }

    @GetMapping("info")
    public Result<FeedbackVO> getFeedback(){
        log.info("用户获取回馈信息");
        return Result.success(feedbackService.getFeedback());
    }

}
