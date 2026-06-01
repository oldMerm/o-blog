package io.github.oldmerman.web.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleGroupLinkMapper {

    @Select("SELECT article_id FROM o_blog.o_article_group_link WHERE group_id = #{groupId}")
    List<Long> selectByGroupId(Long groupId);

    @Delete("DELETE FROM o_blog.o_article_group_link WHERE article_id = #{articleId} and group_id = #{groupId}")
    void unlink(Long articleId, Long groupId);

    @Delete("DELETE FROM o_blog.o_article_group_link WHERE article_id = #{articleId}")
    void unlinkAll(Long articleId);

    @Delete("DELETE FROM o_blog.o_article_group_link WHERE group_id = #{groupId}")
    void unlinkAllByGroupId(Long groupId);

    void insertBatch(Long groupId, List<Long> articleIds);

}
