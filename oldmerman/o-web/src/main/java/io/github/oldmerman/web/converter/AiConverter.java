package io.github.oldmerman.web.converter;

import io.github.oldmerman.model.po.AiConversation;
import io.github.oldmerman.model.vo.AiConversationVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AiConverter {

    AiConversationVO poToVo(AiConversation po);
}
