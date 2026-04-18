package io.github.oldmerman.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.oldmerman.model.dto.ArticleCreateDTO;
import io.github.oldmerman.model.vo.ArticleInfoVO;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文章相关接口
 * @author oldmerman
 * @date 2026-2-17
 */
public interface ArticleService {

    /**
     * 用户获取个人的文章基本信息
     *
     * @return 封装List
     */
    List<ArticleRenderVO> info();

    /**
     * 根据文章类型获取需要渲染的文章
     *
     * @return 文章类型
     */
    List<ArticleRenderVO> getRenderArticle(Byte articleType, Long size) throws JsonProcessingException;

    /**
     * 获取一篇文章，系私有，用于用户预览自己的文章
     *
     * @param id 文章id
     * @return 可访问的临时url
     */
    String getPrivateArticleById(Long id);

    /**
     * 查看文章公共接口
     *
     * @param id 文章id
     * @return url
     */
    ArticleInfoVO getPublicArticleById(Long id);

    /**
     * 上传图片并返回替换URL
     *
     * @param userId  用户id
     * @param paths   待替换路径
     * @param imgList 上传文件
     * @return 替换路径
     */
    List<String> uploadImagesToOSS(Long userId, List<String> paths, List<MultipartFile> imgList);

    /**
     * 上传文章
     *
     * @param userId 用户id
     * @param file   上传的markdown文件
     * @param dto    封装的文章和用户信息
     */
    void upload(Long userId, MultipartFile file, ArticleCreateDTO dto);

    /**
     * 删除文章
     *
     * @param articleName 文章名
     * @param userId      用户唯一id
     */
    void removeArticle(String articleName, Long userId) throws JsonProcessingException;
}
