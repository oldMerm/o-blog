package io.github.oldmerman.web.converter;

import io.github.oldmerman.model.po.User;
import io.github.oldmerman.model.vo.UserInfoVO;
import io.github.oldmerman.model.vo.UserManageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserConverter {

    UserInfoVO poToInfoVO(User user);

    @Mapping(source = "isDelete", target = "status")
    @Mapping(source = "article", target = "articleCount")
    UserManageVO poToManageVO(User user);
}
