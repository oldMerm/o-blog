package io.github.oldmerman.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.common.response.Result;
import io.github.oldmerman.model.dto.ArticleAdminUpdateDTO;
import io.github.oldmerman.model.vo.ArticlePageVO;
import io.github.oldmerman.web.service.ArticleAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/article")
@RequiredArgsConstructor
@Slf4j
public class ArticleAdminController {

    private final ArticleAdminService articleService;

    @GetMapping("page")
    public Result<PageResult<ArticlePageVO>> page(@RequestParam(name = "current", defaultValue = "1") Long current,
                                                  @RequestParam(name = "size", defaultValue = "10") Long size){
        return Result.success(articleService.page(current, size));
    }

    @PostMapping("status")
    public Result<Void> updateArticleStatus(@RequestBody ArticleAdminUpdateDTO dto) throws JsonProcessingException {
        log.info("修改文章:{},状态",dto.getId());
        articleService.updateArticleStatus(dto);
        return Result.success();
    }
}
