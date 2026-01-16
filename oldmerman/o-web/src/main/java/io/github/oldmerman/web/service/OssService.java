package io.github.oldmerman.web.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OssService {

    String uploadUsrImage(Long userId, MultipartFile file);

    String genPreviewUrl(String key);

    List<String> uploadBatch(Long id, List<String> path, List<MultipartFile> files, String bucket);

}
