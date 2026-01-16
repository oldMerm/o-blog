package io.github.oldmerman.web.service.impl;

import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.service.ArticleService;
import io.github.oldmerman.web.service.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;

    private final OssService ossService;

    private static final String BUCKET = "project-oldmerman-artimg";

    public List<String> uploadImagesToOSS(Long userId, List<String> paths, List<MultipartFile> imgList) {
        return ossService.uploadBatch(userId, paths, imgList, BUCKET);
    }
}
