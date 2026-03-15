package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.common.response.ResultCode;
import io.github.oldmerman.model.dto.VersionDTO;
import io.github.oldmerman.model.po.Version;
import io.github.oldmerman.web.converter.VersionConverter;
import io.github.oldmerman.web.mapper.UserMapper;
import io.github.oldmerman.web.mapper.VersionMapper;
import io.github.oldmerman.web.service.VersionService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    private final VersionMapper mapper;

    private final UserMapper userMapper;

    private final VersionConverter converter;

    @Override
    public PageResult<Version> page(Long current, Long size) {
        Page<Version> page = new Page<>(current, size);
        LambdaQueryWrapper<Version> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Version::getCreatedAt);
        IPage<Version> iPage = mapper.selectPage(page, queryWrapper);
        return PageResult.of(
                iPage.getCurrent(),
                iPage.getSize(),
                iPage.getTotal(),
                iPage.getRecords()
        );
    }

    @Override
    public Version getVersionInfo() {
        LambdaQueryWrapper<Version> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Version::getCreatedAt).last("LIMIT 1");
        return mapper.selectOne(queryWrapper);
    }

    @Override
    public void saveVersionInfo(VersionDTO dto) {
        if(ObjectUtils.isEmpty(dto)){
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        Version po = converter.createToVersion(dto);
        po.setObjectName(userMapper.selectNameById(UserContext.getUserId()));
        mapper.insert(po);
    }

    @Override
    public void deleteVersionInfo(Long id) {
        mapper.deleteById(id);
    }

}
