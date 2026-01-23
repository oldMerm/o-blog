package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.dto.ArticlePriDTO;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleMapper extends BaseMapper<Article> {

    @Insert("INSERT INTO o_blog.o_article (id, writer_id, `key`, article_name, article_writer, " +
            "article_decr, article_type, article_status) " +
            "VALUES (#{po.id}, #{po.writerId}, #{po.key}, #{po.articleName}, #{po.articleWriter}, " +
            "#{po.articleDecr}, #{po.articleType}, #{po.articleStatus})")
    void insertPO(@Param("po") Article po);

    @Select("SELECT * FROM o_blog.o_article WHERE writer_id = #{userId}")
    List<Article> selectByUserId(Long userId);

    @Select("SELECT writer_id, `key` FROM o_blog.o_article WHERE id = #{id}")
    ArticlePriDTO getPrivateKeyById(Long id);

    List<ArticleRenderVO> selectArticle(@Param("type") Byte articleType, Long size);

    @Select("SELECT id, article_name, article_status, `like`, created_at FROM o_blog.o_article WHERE article_type = 0")
    List<ArticleRenderVO> selectNotice();
}
