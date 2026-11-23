package io.github.oldmerman.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.oldmerman.model.po.ArticleSearch;

import java.util.List;

public interface ArticleSearchMapper extends BaseMapper<ArticleSearch> {


    List<ArticleSearch> search(String keyword);

    int insertOrIgnoreBatch(List<ArticleSearch> list);
}
