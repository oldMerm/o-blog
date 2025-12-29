package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.User;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {

    @Update("UPDATE o_blog.o_user SET attr = #{key} WHERE id = #{id}")
    void updateUserAttrKey(Long id, String key);

    @Select("SELECT username, article, `like`, is_delete FROM o_blog.o_user WHERE id = #{userId}")
    User selectSimUsrInfo(Long userId);

    @Select("SELECT * FROM o_blog.o_user WHERE id = #{userId}")
    User selectUserById(Long userId);

    @Select("SELECT attr FROM o_blog.o_user WHERE id = #{userId}")
    String getAttrById(Long userId);
}
