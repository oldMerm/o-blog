package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.model.dto.ArticleGroupCreateDTO;
import io.github.oldmerman.model.po.ArticleGroup;
import io.github.oldmerman.model.vo.ArticleGroupRenderVO;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import io.github.oldmerman.web.converter.ArticleConverter;
import io.github.oldmerman.web.mapper.ArticleGroupLinkMapper;
import io.github.oldmerman.web.mapper.ArticleGroupMapper;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.service.ArticleGroupService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleGroupServiceImpl implements ArticleGroupService {

    private final ArticleGroupMapper mapper;

    private final ArticleGroupLinkMapper linkMapper;

    private final ArticleMapper articleMapper;

    private final ArticleConverter articleConverter;

    @Override
    public List<ArticleGroupRenderVO> getRenderGroup() {
        Long userId = UserContext.getUserId();
        return mapper.selectGroupRenderList(userId);
    }

    @Override
    public List<ArticleRenderVO> getArticleByGroup(Long groupId) {
        List<Long> articleIds = linkMapper.selectByGroupId(groupId);
        if(CollectionUtils.isEmpty(articleIds)){
            return null;
        }
        return articleMapper.selectByIds(articleIds)
                .stream()
                .map(articleConverter::poToRenderVO)
                .toList();
    }

    @Override
    public List<ArticleGroupRenderVO> getGroupArticle(Long articleId) {
        if(articleId == null){
            throw new BusinessException(BusErrorCode.ARTICLE_OPRE_FAILED);
        }
        return mapper.selectGroupRenderListByArticleId(articleId);
    }

    @Override
    public void insertArticleGroup(String groupName, String groupDesc) {
        if (groupName == null || groupDesc == null) {
            throw new BusinessException(BusErrorCode.ARTICLE_OPRE_FAILED);
        }
        ArticleGroup po = new ArticleGroup();
        po.setUserId(UserContext.getUserId());
        po.setGroupName(groupName);
        po.setGroupDesc(groupDesc);
        mapper.insert(po);
    }

    @Override
    @Transactional
    public void removeArticleGroup(Long groupId) {
        log.info("删除集合: {}", groupId);
        mapper.deleteById(groupId);
        linkMapper.unlinkAllByGroupId(groupId);
    }

    @Override
    @Transactional
    public void insertArticleInGroup(ArticleGroupCreateDTO dto) {
        Long groupId = dto.getGroupId();
        List<Long> articleIds = dto.getArticleIds();
        if(groupId == null || CollectionUtils.isEmpty(articleIds)){
            throw new BusinessException(BusErrorCode.ARTICLE_OPRE_FAILED);
        }
        log.info("批量插入文章数据至集合: {}", groupId);
        linkMapper.insertBatch(groupId, articleIds);
    }

    @Override
    public void unlinkArticleInGroup(Long articleId, Long groupId) {
        if (articleId == null || groupId == null) {
            throw new BusinessException(BusErrorCode.ARTICLE_OPRE_FAILED);
        }
        linkMapper.unlink(articleId, groupId);
    }

}
