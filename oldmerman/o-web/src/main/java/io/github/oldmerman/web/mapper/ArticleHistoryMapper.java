package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.ArticleHistory;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface ArticleHistoryMapper extends BaseMapper<ArticleHistory> {

    @Select("SELECT id FROM o_blog.o_article_history WHERE user_id = #{userId} AND article_id = #{articleId}")
    Integer selectByUsrArtId(Long userId, Long articleId);

    @Update("UPDATE o_blog.o_article_history SET updated_at = #{now} WHERE id = #{id}")
    void updateHistoryTime(int id, LocalDateTime now);
}
