package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.oldmerman.model.po.ArticleHistory;
import io.github.oldmerman.model.vo.ArticleHistoryVO;
import io.github.oldmerman.web.mapper.ArticleHistoryMapper;
import io.github.oldmerman.web.service.ArticleHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleHistoryServiceImpl implements ArticleHistoryService {

    private final ArticleHistoryMapper mapper;

    @Override
    public List<ArticleHistoryVO> getArticleHistory(Long userId) {
        LambdaQueryWrapper<ArticleHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleHistory::getUserId, userId).orderByDesc(ArticleHistory::getUpdatedAt);
        return mapper.selectList(wrapper).stream()
                .map(i -> {
                    ArticleHistoryVO vo = new ArticleHistoryVO();
                    vo.setUserId(String.valueOf(i.getUserId()));
                    vo.setArticleId(String.valueOf(i.getArticleId()));
                    vo.setArticleName(i.getArticleName());
                    vo.setUpdatedAt(i.getUpdatedAt());
                    return vo;
                }).toList();
    }

    @Override
    public void removeArticleHistory(Long userId) {
        if (userId != null) {
            mapper.deleteArticleHistory(userId);
        }
    }
}
