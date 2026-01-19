package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.dto.UserManageDTO;
import io.github.oldmerman.model.po.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {

    @Update("UPDATE o_blog.o_user SET attr = #{key} WHERE id = #{id}")
    void updateUserAttrKey(Long id, String key);

    @Select("SELECT id, username, article, `like`, attr, is_delete FROM o_blog.o_user WHERE id = #{userId}")
    User selectSimUsrInfo(Long userId);

    @Select("SELECT * FROM o_blog.o_user WHERE id = #{userId}")
    User selectUserById(Long userId);

    void updateUserInfo(@Param("dto") UserManageDTO dto);

    @Update("UPDATE o_blog.o_user SET is_delete = 2 WHERE id = #{userId}")
    void logicDeleteUser(Long userId);

    @Select("SELECT username FROM o_blog.o_user WHERE id = #{userId}")
    String selectNameById(Long userId);
}
