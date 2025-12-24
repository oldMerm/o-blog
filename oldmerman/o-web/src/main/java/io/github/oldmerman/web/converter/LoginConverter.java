package io.github.oldmerman.web.converter;

import io.github.oldmerman.model.dto.UserCreatedDTO;
import io.github.oldmerman.model.po.UserPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoginConverter {

    UserPO createToUserPO(UserCreatedDTO dto);
}
