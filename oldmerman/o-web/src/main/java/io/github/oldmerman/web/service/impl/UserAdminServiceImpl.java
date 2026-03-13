package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.oldmerman.common.constant.BusinessMap;
import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.dto.UserToggleDTO;
import io.github.oldmerman.model.po.User;
import io.github.oldmerman.model.vo.UserManageVO;
import io.github.oldmerman.web.converter.UserConverter;
import io.github.oldmerman.web.mapper.UserMapper;
import io.github.oldmerman.web.service.OssService;
import io.github.oldmerman.web.service.UserAdminService;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserMapper userMapper;

    private final OssService ossService;

    private final UserConverter converter;

    @Override
    public PageResult<UserManageVO> page(Long current, Long size) {
        Page<User> page = new Page<>(current, size);
        IPage<User> IPage = userMapper.selectPage(page, null);
        List<UserManageVO> voList = IPage.getRecords().stream()
                .map(item -> {
                    UserManageVO vo = converter.poToManageVO(item);
                    vo.setAttr(ossService.genPreviewURL(item.getAttr(), null));
                    return vo;
                })
                .toList();
        return PageResult.of(
                IPage.getCurrent(),
                IPage.getSize(),
                IPage.getTotal(),
                voList
        );
    }

    @Override
    public String getUserPermission() {
        User user = userMapper.selectUserById(UserContext.getUserId());
        return BusinessMap.USER_TYPE_MAP.get(user.getType());
    }

    @Override
    public void toggleUserStatus(UserToggleDTO dto) {
        userMapper.toggleUserStatus(dto);
    }

}
