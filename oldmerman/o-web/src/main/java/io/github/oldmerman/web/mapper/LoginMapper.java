package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.UserPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface LoginMapper extends BaseMapper<UserPO> {

    @Insert("INSERT INTO o_blog.o_user (id, username, email, password)" +
            "VALUES(#{id}, #{username},#{email},#{password})")
    void createUser(UserPO po);

    @Select("SELECT id FROM o_blog.o_user WHERE username = #{username} AND password = #{password} AND is_delete = 1")
    Long verifyUserInfo(String username, String password);

    @Update("UPDATE o_blog.o_user SET is_delete = 2 WHERE id = #{userId}")
    void logoffByUserId(Long userId);
}
