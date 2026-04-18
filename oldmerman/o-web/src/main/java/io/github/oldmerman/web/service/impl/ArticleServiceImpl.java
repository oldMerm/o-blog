package io.github.oldmerman.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.common.constant.RedisPrefix;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.enums.NumEnum;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.common.util.IdGenerator;
import io.github.oldmerman.model.dto.ArticleCreateDTO;
import io.github.oldmerman.model.dto.ArticlePriDTO;
import io.github.oldmerman.model.po.Article;
import io.github.oldmerman.model.po.ArticleHistory;
import io.github.oldmerman.model.po.ArticleImage;
import io.github.oldmerman.model.vo.ArticleInfoVO;
import io.github.oldmerman.model.vo.ArticleRenderVO;
import io.github.oldmerman.web.converter.ArticleConverter;
import io.github.oldmerman.web.mapper.ArticleHistoryMapper;
import io.github.oldmerman.web.mapper.ArticleImageMapper;
import io.github.oldmerman.web.mapper.ArticleMapper;
import io.github.oldmerman.web.mapper.UserMapper;
import io.github.oldmerman.web.service.ArticleService;
import io.github.oldmerman.web.service.OssService;
import io.github.oldmerman.web.util.PathUtils;
import io.github.oldmerman.web.util.RedisUtils;
import io.github.oldmerman.web.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;

    private final ArticleImageMapper articleImageMapper;

    private final UserMapper userMapper;

    private final ArticleHistoryMapper historyMapper;

    private final OssService ossService;

    private final ArticleConverter converter;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    @Value("${alias.oss.pub-bucket}")
    public String BUCKET;

    @Override
    public List<ArticleRenderVO> info() {
        Long userId = UserContext.getUserId();
        List<Article> poList = articleMapper.selectByUserId(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return poList.stream()
                .map(item -> {
                    ArticleRenderVO vo = converter.poToRenderVO(item);
                    vo.setId(item.getId().toString());
                    vo.setCreatedAt(item.getCreatedAt().format(formatter));
                    return vo;
                }).toList();
    }

    @Override
    public List<ArticleRenderVO> getRenderArticle(Byte articleType, Long size) throws JsonProcessingException {
        String data = redisTemplate.opsForValue().get(RedisPrefix.ARTICLE_RENDER + articleType);
        if (!ObjectUtils.isEmpty(data)) {
                return objectMapper.readValue(data, new TypeReference<>() {
            });
        }
        List<ArticleRenderVO> vo;
        if (articleType == 0) {
            vo = articleMapper.selectNotice();
        } else {
            vo = articleMapper.selectArticle(articleType, size);
        }
        redisTemplate.opsForValue().set(RedisPrefix.ARTICLE_RENDER + articleType, objectMapper.writeValueAsString(vo),
                NumEnum.ARTICLE_EXPIRE_TIME.getValue(), TimeUnit.DAYS);
        return vo;
    }

    @Override
    public String getPrivateArticleById(Long id) {
        ArticlePriDTO dto = articleMapper.getPrivateKeyById(id);
        String key = dto.getKey();
        if (key == null || !Objects.equals(UserContext.getUserId(), dto.getWriterId())) {
            throw new BusinessException(BusErrorCode.FILE_UNEXIST);
        }
        return ossService.genPreviewURL(key, null);
    }

    @Override
    public ArticleInfoVO getPublicArticleById(Long id) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getArticleStatus, Article.ArticleStatus.PUBLISHED).eq(Article::getId, id);
        Article article = articleMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(article)) {
            throw new BusinessException(BusErrorCode.ARTICLE_WAS_REMOVED);
        }
        String key = article.getKey();
        if (key == null) {
            throw new BusinessException(BusErrorCode.FILE_UNEXIST);
        }
        // 记录用户浏览历史
        Long userId = UserContext.getUserId();
        if(userId != null){
            Integer historyId = historyMapper.selectByUsrArtId(userId, article.getId());
            if(historyId != null){
                historyMapper.updateHistoryTime(historyId, LocalDateTime.now());
            }else{
                ArticleHistory ah = new ArticleHistory();
                ah.setUserId(userId);
                ah.setArticleId(article.getId());
                ah.setArticleName(article.getArticleName());
                historyMapper.insert(ah);
            }
        }

        ArticleInfoVO articleInfoVO = converter.poToInfoVO(article);
        articleInfoVO.setUrl(ossService.genPreviewURL(key, null));
        return articleInfoVO;
    }

    @Override
    public List<String> uploadImagesToOSS(Long userId, List<String> paths, List<MultipartFile> imgList) {
        List<String> keys = ossService.uploadBatch(userId, paths, imgList, BUCKET);
        return ossService.genPublicURL(keys, BUCKET);
    }

    @Override
    @Transactional
    public void upload(Long userId, MultipartFile file, ArticleCreateDTO dto) {
        if(articleMapper.exist(dto.getArticleName()) == 1){
            // 存在文章（文章同名）
            throw new BusinessException(BusErrorCode.ARTICLE_NAME_EXIST);
        }
        // 1.上传文件并获取key
        String mdKey = ossService.uploadMd(userId, file);
        if (ObjectUtils.isEmpty(dto)) {
            throw new BusinessException(BusErrorCode.FILE_DECR_UNEXIST);
        }
        // 2.构建文章对象
        Article po = converter.createToPO(dto);
        Long articleId = IdGenerator.nextId();
        po.setId(articleId);
        po.setWriterId(userId);
        po.setArticleStatus(Article.ArticleStatus.UNDER_REVIEW);
        po.setArticleWriter(userMapper.selectNameById(userId));
        po.setKey(mdKey);
        // 3.构建图片对象
        List<String> attrs = dto.getAttrs();
        if (attrs != null) {
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
            articleImageMapper.insert(images);
        }
        // 4.插入数据库
        articleMapper.insertPO(po);
        // 5.用户加入缓存
        redisTemplate.opsForValue().set(RedisPrefix.ARTICLE_SUBMIT + userId, articleId.toString(), 1440, TimeUnit.MINUTES);

    }

    @Override
    @Transactional
    public void removeArticle(String articleName, Long userId) throws JsonProcessingException {
        LambdaQueryWrapper<Article> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Article::getWriterId, userId).eq(Article::getArticleName, articleName);
        Article article = articleMapper.selectOne(lambdaQueryWrapper);
        if (ObjectUtils.isEmpty(article)) {
            throw new BusinessException(BusErrorCode.FILE_UNEXIST);
        }
        Long articleId = article.getId();
        articleMapper.deleteById(article.getId());

        List<ArticleImage> articleImages = articleImageMapper.selectByArticleId(articleId);
        articleImageMapper.deleteByIds(articleImages.stream().map(ArticleImage::getId).toList());
        if (!articleImages.isEmpty()) {
            List<String> keys = PathUtils.getOssPubKeys(articleImages.stream()
                    .map(ArticleImage::getUrl)
                    .toList());
            // 到oss删除key
            ossService.deleteBatch(keys, BUCKET);
            ossService.deleteOne(article.getKey(), null);
        }

        // 重构缓存
        RedisUtils.rebuildArticleRenderCache(article.getArticleType(), articleId, redisTemplate, objectMapper);
    }

}
