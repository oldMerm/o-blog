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
        LambdaQueryWrapper<ArticleLikeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLikeRecord::getUserId, userId);
        queryWrapper.eq(ArticleLikeRecord::getArticleId, articleId);
        return mapper.exists(queryWrapper);
    }

    @Override
    @Transactional
    public Boolean like(Long userId, Long articleId) {
        int c = mapper.insertIgnore(userId, articleId);
        if(c > 0){
            log.info("用户: {} 点赞文章 {}", userId, articleId);
            articleMapper.incrLikeRecords(articleId, 1);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Boolean unlike(Long userId, Long articleId){
        int c = mapper.deleteByUserAndArticle(userId, articleId);
        if(c > 0){
            log.info("用户: {} 取消点赞文章 {}", userId, articleId);
            articleMapper.incrLikeRecords(articleId, -1);
            return true;
        }
        return false;
    }

}
