package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.Article;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface ArticleMapper extends BaseMapper<Article> {

    @Insert("INSERT INTO o_blog.o_article (id, writer_id, `key`, article_name, article_writer, " +
            "article_decr, article_type, article_status) " +
            "VALUES (#{po.id}, #{po.writerId}, #{po.key}, #{po.articleName}, #{po.articleWriter}, " +
            "#{po.articleDecr}, #{po.articleType}, #{po.articleStatus})")
    void insertPO(@Param("po") Article po);
}
