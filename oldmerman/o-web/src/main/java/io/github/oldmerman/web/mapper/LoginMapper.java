package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface LoginMapper extends BaseMapper<User> {

    @Insert("INSERT INTO o_blog.o_user (id, username, email, password)" +
            "VALUES(#{id}, #{username},#{email},#{password})")
    void createUser(User po);

    @Select("SELECT id FROM o_blog.o_user WHERE email = #{email} AND password = #{password} AND is_delete = 1")
    Long verifyUserInfoByEmail(String username, String email);

    @Select("SELECT id FROM o_blog.o_user WHERE username = #{username} AND password = #{password} AND is_delete = 1")
    Long verifyUserInfoById(String username, String password);

    @Update("UPDATE o_blog.o_user SET is_delete = 2 WHERE id = #{userId}")
    void logoffByUserId(Long userId);
}
