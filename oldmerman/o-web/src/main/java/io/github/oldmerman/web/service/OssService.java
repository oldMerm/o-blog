package io.github.oldmerman.web.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OssService {

    String uploadUsrImage(Long userId, MultipartFile file);

    /* 领域服务 */
    String genPreviewURL(String key, String bucket);

    List<String> genPublicURL(List<String> keys, String bucket);

    List<String> uploadBatch(Long id, List<String> path, List<MultipartFile> files, String bucket);

    String uploadMd(Long id, MultipartFile file);

    void deleteOne(String key, String bucket);

    void deleteBatch(List<String> keys, String bucket);
}
