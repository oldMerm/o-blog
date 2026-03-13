package io.github.oldmerman.web.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * oss相关接口(阿里)
 * @author oldmerman
 * @date 2026-12-30
 */
public interface OssService {

    /**
     * 后端转存用户头像，限定格式
     *
     * @param userId 用户id
     * @param file   文件
     * @return 唯一key
     */
    String uploadUsrImage(Long userId, MultipartFile file);

    /**
     * 生成闲时预览的URL
     *
     * @return url
     */
    String genPreviewURL(String key, String bucket);

    /**
     * 生成公共url，只生产一次
     *
     * @param keys   文件路径
     * @param bucket 所属bucket
     * @return 处理后可直接访问的URL
     */
    List<String> genPublicURL(List<String> keys, String bucket);

    /**
     * 通用方法，批量上传图片
     *
     * @param files 图片集合
     * @return 图片key集合
     */
    List<String> uploadBatch(Long id, List<String> path, List<MultipartFile> files, String bucket);

    /**
     * 上传md文档
     *
     * @return 唯一key
     */
    String uploadMd(Long id, MultipartFile file);

    /**
     * @param key    需删除key
     * @param bucket 所属bucket
     */
    void deleteOne(String key, String bucket);

    /**
     * 批量删除key
     *
     * @param keys   key集合
     * @param bucket 所属bucket
     */
    void deleteBatch(List<String> keys, String bucket);
}
