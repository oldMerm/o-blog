package io.github.oldmerman.web.converter;

import io.github.oldmerman.model.dto.UserCreatedDTO;
import io.github.oldmerman.model.po.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoginConverter {

    User createToUserPO(UserCreatedDTO dto);
}
