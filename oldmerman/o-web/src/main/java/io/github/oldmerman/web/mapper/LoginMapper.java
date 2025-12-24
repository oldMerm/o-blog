package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.UserPO;
import org.apache.ibatis.annotations.Insert;

public interface LoginMapper extends BaseMapper<UserPO> {

    @Insert("INSERT INTO o_blog.o_user (id, username, email, password)" +
            "VALUES(#{id}, #{username},#{email},#{password})")
    void createUser(UserPO po);
}
