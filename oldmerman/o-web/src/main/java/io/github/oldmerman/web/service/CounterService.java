package io.github.oldmerman.web.service;

import io.github.oldmerman.model.po.Counter;
import io.github.oldmerman.model.vo.ArticleRenderVO;

import java.util.List;

/**
 * 统计功能相关接口
 * @author oldmerman
 * @date 2026-2-15
 */
public interface CounterService {

    /**
     * 获取网页运行时间
     *
     * @return 网页运行天数
     */
    Integer getSystemTime();

    /**
     * 获取相关信息五个月的增量
     *
     * @return 增量的集合
     */
    List<Counter> getIncr(Long type);

    /**
     * 获取更新信息
     *
     * @return 文章渲染对象
     */
    ArticleRenderVO getArticleUpdateInfo();


}
