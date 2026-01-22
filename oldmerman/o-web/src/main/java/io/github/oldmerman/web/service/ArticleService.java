package io.github.oldmerman.web.service;

import io.github.oldmerman.model.dto.ArticleCreateDTO;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArticleService {

    List<String> uploadImagesToOSS(Long userId, List<String> paths, List<MultipartFile> imgList);

    String getPrivateArticleById(Long id);

    void upload(Long userId, MultipartFile file, ArticleCreateDTO dto);

    List<ArticleRenderVO> info();
}
