package io.github.oldmerman.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.oldmerman.model.dto.ArticleCreateDTO;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArticleService {

    List<ArticleRenderVO> getRenderArticle(Byte articleType, Long size) throws JsonProcessingException;

    List<ArticleRenderVO> info();

    String getPrivateArticleById(Long id);

    List<String> uploadImagesToOSS(Long userId, List<String> paths, List<MultipartFile> imgList);

    void upload(Long userId, MultipartFile file, ArticleCreateDTO dto);

}
