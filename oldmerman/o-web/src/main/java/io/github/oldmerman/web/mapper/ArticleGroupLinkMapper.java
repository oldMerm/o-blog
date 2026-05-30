package io.github.oldmerman.web.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

public interface ArticleGroupLinkMapper {

    @Insert("INSERT INTO o_blog.o_article_group_link (group_id, article_id) VALUES (#{articleId}, #{groupId})")
    void insert(Long articleId, Long groupId);

    @Delete("DELETE FROM o_blog.o_article_group_link WHERE article_id = #{articleId} and group_id = #{groupId}")
    void unlink(Long articleId, Long groupId);

    @Delete("DELETE FROM o_blog.o_article_group_link WHERE article_id = #{articleId}")
    void unlinkAll(Long articleId);
}
