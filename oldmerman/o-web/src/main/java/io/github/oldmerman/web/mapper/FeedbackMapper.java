package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.FeedBack;
import io.github.oldmerman.model.vo.FeedbackVO;
import org.apache.ibatis.annotations.Select;

public interface FeedbackMapper extends BaseMapper<FeedBack> {

    @Select("SELECT id, feedback_type, feedback, reply FROM o_blog.o_feedback WHERE user_id = #{userId}")
    FeedbackVO selectByUserId(Long userId);
}
