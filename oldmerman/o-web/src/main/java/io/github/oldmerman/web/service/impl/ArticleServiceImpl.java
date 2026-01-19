package io.github.oldmerman.web.service.impl;

import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.util.IdGenerator;
import io.github.oldmerman.model.dto.ArticleCreateDTO;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.model.po.ArticleImage;
import io.github.oldmerman.web.converter.ArticleConverter;
import io.github.oldmerman.web.mapper.ArticleImageMapper;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.mapper.UserMapper;
import io.github.oldmerman.web.service.ArticleService;
import io.github.oldmerman.web.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;

    private final ArticleImageMapper articleImageMapper;

    private final UserMapper userMapper;

    private final OssService ossService;

    private final ArticleConverter converter;

    private static final String BUCKET = "project-oldmerman-artimg";

    /**
     * 上传图片并返回替换URL
     * @param userId 用户id
     * @param paths 待替换路径
     * @param imgList 上传文件
     * @return 替换路径
     */
    public List<String> uploadImagesToOSS(Long userId, List<String> paths, List<MultipartFile> imgList) {
        List<String> keys = ossService.uploadBatch(userId, paths, imgList, BUCKET);
        return ossService.genPublicURL(keys, BUCKET);
    }

    @Transactional
    public void upload(Long userId, MultipartFile file, ArticleCreateDTO dto) {
        // 1.上传文件并获取key
        String mdKey = ossService.uploadMd(userId, file);
        if(ObjectUtils.isEmpty(dto)){
            throw new BusinessException(BusErrorCode.FILE_DECR_UNEXIST);
        }
        // 2.构建文章对象
        Article po = converter.createToPO(dto);
        Long articleId = IdGenerator.nextId();
        po.setId(articleId);
        po.setArticleStatus(Article.ArticleStatus.UNDER_REVIEW);
        po.setWriterId(userId);
        po.setArticleWriter(userMapper.selectNameById(userId));
        po.setKey(mdKey);
        // 3.构建图片对象
        List<String> attrs = dto.getAttrs();
        List<ArticleImage> images = attrs.stream()
                .filter(s -> s != null && !s.isEmpty())
                .map(item -> {
                    ArticleImage img = new ArticleImage();
                    img.setId(IdGenerator.nextId());
                    img.setArticleId(articleId);
                    img.setUrl(item);
                    return img;
                })
                .toList();
        // 4.插入数据库
        articleMapper.insert(po);
        articleImageMapper.insert(images);
        // 5.事务回滚
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if(status == TransactionSynchronization.STATUS_ROLLED_BACK){
                    log.warn("事务回滚后，删除所有存在key");
                    // 插入失败，到oss删除所有的key
                    ossService.deleteOne(mdKey, null);
                    ossService.deleteBatch(attrs, BUCKET);
                }
            }
        });
    }
}
