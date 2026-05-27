package io.github.oldmerman.web.service.impl;

import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.model.po.ArticleGroup;
import io.github.oldmerman.web.mapper.ArticleGroupLinkMapper;
import io.github.oldmerman.web.mapper.ArticleGroupMapper;
import io.github.oldmerman.web.service.ArticleGroupService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class ArticleGroupServiceImpl implements ArticleGroupService {

    private final ArticleGroupMapper mapper;

    private final ArticleGroupLinkMapper linkMapper;

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
        if(articleId == null || groupId == null){
            throw new BusinessException(ResultCode.FAIL);
        }
        ArticleGroup articleGroup = mapper.selectById(groupId);
        if(!ObjectUtils.isEmpty(articleGroup)){
            linkMapper.insert(articleId, groupId);
        }
    }
}
