package io.github.oldmerman.web.converter;

import io.github.oldmerman.model.po.User;
import io.github.oldmerman.model.vo.UserInfoVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConverter {

    UserInfoVO poToInfoVO(User user);
}
