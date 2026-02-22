package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.Feedback;
import io.github.oldmerman.model.vo.FeedbackVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FeedbackMapper extends BaseMapper<Feedback> {

    @Select("SELECT id, feedback_type, feedback, reply FROM o_blog.o_feedback WHERE user_id = #{userId}")
    FeedbackVO selectByUserId(Long userId);

    @Select("SELECT * FROM o_blog.o_feedback WHERE user_id = #{userId}")
    List<Feedback> selectBatchByUserId(Long userId);

}
