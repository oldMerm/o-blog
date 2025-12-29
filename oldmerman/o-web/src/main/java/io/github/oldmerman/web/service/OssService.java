package io.github.oldmerman.web.service;

import io.github.oldmerman.model.vo.OssPutVO;
import org.springframework.web.multipart.MultipartFile;

public interface OssService {

    String uploadUsrImage(Long userId, MultipartFile file);

    String genPreviewUrl(String key);
}
