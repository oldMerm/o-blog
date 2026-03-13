package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.ArticleImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleImageMapper extends BaseMapper<ArticleImage> {

    @Delete("DELETE FROM o_blog.o_article_img WHERE article_id = #{articleId}")
    void deleteByArticleId(Long articleId);

    @Select("SELECT * FROM o_blog.o_article_img WHERE article_id = #{articleId}")
    List<ArticleImage> selectByArticleId(Long articleId);

    List<String> selectDanglingRecord();

}
