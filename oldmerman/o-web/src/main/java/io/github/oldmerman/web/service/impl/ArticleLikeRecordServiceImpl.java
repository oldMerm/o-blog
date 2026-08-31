package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.oldmerman.model.po.ArticleLikeRecord;
import io.github.oldmerman.web.mapper.ArticleLikeRecordMapper;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.service.ArticleLikeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleLikeRecordServiceImpl implements ArticleLikeRecordService {

    private final ArticleLikeRecordMapper mapper;

    private final ArticleMapper articleMapper;

    @Override
    public Boolean getLikeRecord(Long userId, Long articleId) {
        return isExistRecord(userId, articleId);
    }

    @Override
    @Transactional
    public void saveLikeRecord(Long userId, Long articleId) {
        if (isExistRecord(userId, articleId)) {
            return; // 存在直接返回
        }
        // 不存在，新增点赞条目
        ArticleLikeRecord articleLikeRecord = new ArticleLikeRecord();
        articleLikeRecord.setUserId(userId);
        articleLikeRecord.setArticleId(articleId);
        mapper.insert(articleLikeRecord);
        articleMapper.incrLikeRecords(articleId);
    }

    private boolean isExistRecord(Long userId, Long articleId) {
        LambdaQueryWrapper<ArticleLikeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLikeRecord::getUserId, userId);
        queryWrapper.eq(ArticleLikeRecord::getArticleId, articleId);
        return mapper.exists(queryWrapper);
    }
}
