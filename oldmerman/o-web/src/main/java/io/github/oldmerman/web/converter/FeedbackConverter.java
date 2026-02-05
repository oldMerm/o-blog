package io.github.oldmerman.web.converter;

import io.github.oldmerman.model.po.FeedBack;
import io.github.oldmerman.model.vo.FeedbackVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackConverter {

    FeedbackVO poToVo(FeedBack po);
}
