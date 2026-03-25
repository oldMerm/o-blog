package io.github.oldmerman.web.converter;

import io.github.oldmerman.model.po.AiConversation;
import io.github.oldmerman.model.po.AiMessages;
import io.github.oldmerman.model.vo.AiConversationVO;
import io.github.oldmerman.model.vo.AiMessagesVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AiConverter {

    AiConversationVO poToVo(AiConversation po);

    AiMessagesVO messagePoToVo(AiMessages po);
}
