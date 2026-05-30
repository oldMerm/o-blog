package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.ArticleGroup;
import io.github.oldmerman.model.vo.ArticleGroupRenderVO;

import java.util.List;

public interface ArticleGroupMapper extends BaseMapper<ArticleGroup> {

    List<ArticleGroupRenderVO> selectGroupRenderList(Long userId);
}
