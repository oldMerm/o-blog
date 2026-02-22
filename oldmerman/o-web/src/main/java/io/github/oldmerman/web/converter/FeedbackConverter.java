package io.github.oldmerman.web.converter;

import io.github.oldmerman.model.po.Feedback;
import io.github.oldmerman.model.vo.FeedbackVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackConverter {

    FeedbackVO poToVo(Feedback po);
}
