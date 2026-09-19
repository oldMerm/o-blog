package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.ArticleLikeRecord;
import org.apache.ibatis.annotations.Delete;

public interface ArticleLikeRecordMapper extends BaseMapper<ArticleLikeRecord> {

    int insertIgnore(Long userId, Long articleId);

    @Delete("DELETE FROM o_blog.o_article_like_record WHERE user_id = #{userId} AND article_id = #{articleId}")
    int deleteByUserAndArticle(Long userId, Long articleId);
}
