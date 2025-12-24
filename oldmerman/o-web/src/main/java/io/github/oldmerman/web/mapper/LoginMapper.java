package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.UserPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface LoginMapper extends BaseMapper<UserPO> {

    @Insert("INSERT INTO o_blog.o_user (id, username, email, password)" +
            "VALUES(#{id}, #{username},#{email},#{password})")
    void createUser(UserPO po);

    @Select("SELECT id FROM o_blog.o_user WHERE username = #{username} AND password = #{password}")
    Long verifyUserInfo(String username, String password);
}
