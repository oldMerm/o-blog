package io.github.oldmerman.web.service;

import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.model.dto.VersionDTO;
import io.github.oldmerman.model.po.Version;

public interface VersionService {

    /**
     * 分页查询版本历史
     *
     * @param current 起始页数
     * @param size    每页条数
     * @return 分页集合
     */
    PageResult<Version> page(Long current, Long size);

    /**
     * 获取版本信息
     *
     * @return 最近一次的版本信息
     */
    Version getVersionInfo();

    /**
     * 更新版本信息
     *
     * @param dto 封装dto
     */
    void saveVersionInfo(VersionDTO dto);

    /**
     * 删除版本信息
     *
     * @param id 版本id
     */
    void deleteVersionInfo(Long id);
}
