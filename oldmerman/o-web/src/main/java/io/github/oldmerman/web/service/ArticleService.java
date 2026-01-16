package io.github.oldmerman.web.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArticleService {

    List<String> uploadImagesToOSS(Long userId, List<String> paths, List<MultipartFile> imgList);
}
