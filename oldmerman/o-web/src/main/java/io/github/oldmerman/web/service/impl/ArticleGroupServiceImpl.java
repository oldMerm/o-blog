package io.github.oldmerman.web.service.impl;

import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.model.po.ArticleGroup;
import io.github.oldmerman.model.vo.ArticleGroupRenderVO;
import io.github.oldmerman.web.mapper.ArticleGroupLinkMapper;
import io.github.oldmerman.web.mapper.ArticleGroupMapper;
import io.github.oldmerman.web.service.ArticleGroupService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleGroupServiceImpl implements ArticleGroupService {

    private final ArticleGroupMapper mapper;

    private final ArticleGroupLinkMapper linkMapper;

    @Override
    public List<ArticleGroupRenderVO> getRenderGroup() {
        Long userId = UserContext.getUserId();
        return mapper.selectGroupRenderList(userId);
    }

    @Override
    public void insertArticleGroup(String groupName, String groupDesc) {
        if(groupName == null || groupDesc == null){
            throw new BusinessException(ResultCode.FAIL);
        }
        ArticleGroup po = new ArticleGroup();
        po.setUserId(UserContext.getUserId());
        po.setGroupName(groupName);
        po.setGroupName(groupDesc);
        mapper.insert(po);
    }

    @Override
    public void insertArticleInGroup(Long articleId, Long groupId) {
        checkLinkParam(articleId, groupId);
        ArticleGroup articleGroup = mapper.selectById(groupId);
        if(!ObjectUtils.isEmpty(articleGroup)){
            linkMapper.insert(articleId, groupId);
        } else {
            throw new BusinessException(ResultCode.FAIL);
        }
    }

    @Override
    public void unlinkArticleInGroup(Long articleId, Long groupId) {
        checkLinkParam(articleId, groupId);
        linkMapper.unlink(articleId, groupId);
    }

    private void checkLinkParam(Long articleId, Long groupId){
        if(articleId == null || groupId == null){
            throw new BusinessException(ResultCode.FAIL);
        }
    }
}
