package io.github.oldmerman.web.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {

    String uploadUsrImage(Long userId, MultipartFile file);

    String genPreviewUrl(String key);
}
