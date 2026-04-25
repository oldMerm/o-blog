package io.github.oldmerman.web.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.enums.NumEnum;
import io.github.oldmerman.common.enums.WebEnum;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.web.mapper.UserMapper;
import io.github.oldmerman.web.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OssServiceImpl implements OssService {

    private final UserMapper userMapper;

    private final OSS ossClient;

    @Value("${alias.oss.pri-bucket}")
    private String BUCKET;

    // 定义允许的格式常量，方便维护
    private static final List<String> IMG_ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private static final List<String> MD_ALLOWED_EXTENSIONS = List.of("md");

    @Override
    @Transactional
    public String uploadUsrImage(Long userId, MultipartFile file) {
        // 校验文件扩展名
        String ext = getAllowExt(file, IMG_ALLOWED_EXTENSIONS);

        // 准备新文件Key和旧文件Key
        String newFileKey = genFileName(userId.toString(), ext, WebEnum.USER_PREFIX.getValue());
        String oldFileKey = userMapper.selectUserById(userId).getAttr();

        try {
            // 上传到 OSS
            ossClient.putObject(BUCKET, newFileKey, file.getInputStream());

            // 更新数据库 (建议加上事务注解 @Transactional 在 Service 层)
            userMapper.updateUserAttrKey(userId, newFileKey);

            // 异步或尝试删除旧图片（非核心业务，即使失败也不应影响主流程）
            ossClient.deleteObject(BUCKET, oldFileKey);

            return newFileKey;

        } catch (IOException | com.aliyun.oss.OSSException e) {
            // 上传或流读取失败，记录日志
            log.error("用户:{} 头像上传失败: {}", userId, e.getMessage(), e);
            throw new BusinessException(BusErrorCode.UPLOAD_FAILED);
        }
    }

    @Override
    public String genPreviewURL(String key, String bucket) {
        String newBucket = bucket == null ? BUCKET : bucket;
        Date expires = new Date(System.currentTimeMillis() + NumEnum.USER_ATTR_EXPIRE.getValue() * 1000);
        if (!StringUtils.hasText(key)) {
            return null;
        }
        URL url = ossClient.generatePresignedUrl(newBucket, key, expires);
        return url.toString();
    }

    @Override
    public List<String> genPublicURL(List<String> keys, String bucket) {
        String newBucket = bucket == null ? BUCKET : bucket;
        if (keys.isEmpty()) {
            throw new BusinessException(BusErrorCode.UPLOAD_FAILED);
        }
        return keys.stream().map(key -> "https://" + newBucket + ".oss-cn-guangzhou.aliyuncs.com/" + key).toList();
    }

    @Override
    public List<String> uploadBatch(Long id, List<String> path,
                                    List<MultipartFile> files, String bucket) {
        List<String> batchList = new ArrayList<>();
        int size, i;
        if ((size = path.size()) != files.size()) {
            throw new BusinessException(BusErrorCode.FILE_LEN_FAILED);
        }
        try {
            for (i = 0; i < size; i++) {
                MultipartFile file = files.get(i);
                // 校验文件扩展名
                String ext = getAllowExt(file, IMG_ALLOWED_EXTENSIONS);
                String key = genFileName(RandomUtil.randomString(16), ext, WebEnum.MD_IMG_PREFIX.getValue());
                if (key.contains("/./")) {
                    key = Paths.get("", key.split("/")).normalize().toString().replace("\\", "/");
                }
                batchList.add(key);
                ossClient.putObject(bucket, key, file.getInputStream());
            }
        } catch (IOException | com.aliyun.oss.OSSException e) {
            // 上传或流读取失败，记录日志
            log.error("图片上传失败: {}", e.getMessage(), e);
            throw new BusinessException(BusErrorCode.UPLOAD_FAILED);
        }
        return batchList;
    }

    @Override
    public String uploadMd(Long id, MultipartFile file) {
        String ext = getAllowExt(file, MD_ALLOWED_EXTENSIONS);
        String key = genFileName(id.toString(), ext, WebEnum.MD_PREFIX.getValue());
        try {
            ossClient.putObject(BUCKET, key, file.getInputStream());
        } catch (IOException | com.aliyun.oss.OSSException e) {
            // 上传或流读取失败，记录日志
            log.error("{},md文件上传失败: {}", id, e.getMessage(), e);
            throw new BusinessException(BusErrorCode.UPLOAD_FAILED);
        }
        return key;
    }

    @Override
    public void deleteOne(String key, String bucket) {
        bucket = bucket == null ? BUCKET : bucket;
        try {
            ossClient.deleteObject(bucket, key);
        } catch (Exception e) {
            log.warn("删除Key失败: {}, 原因: {}", key, e.getMessage());
        }
    }

    @Override
    public void deleteBatch(List<String> keys, String bucket) {
        bucket = bucket == null ? BUCKET : bucket;
        try {
            ossClient.deleteObjects(
                    new DeleteObjectsRequest(bucket)
                            .withKeys(keys).withQuiet(true));
        } catch (Exception e) {
            log.warn("删除Keys失败: {}, 原因: {}", keys, e.getMessage());
        }
    }

    /**
     * 安全获取对应文件拓展名
     *
     * @param file 文件
     * @param flag 文件集合
     * @return ext 文件拓展名
     */
    private String getAllowExt(MultipartFile file, List<String> flag) {
        String originalFilename = Optional.of(file.getOriginalFilename())
                .orElseThrow(() -> new BusinessException(BusErrorCode.FILE_EXT_FAILED));
        // 获取扩展名
        String ext = StringUtils.getFilenameExtension(originalFilename);
        // 校验格式（忽略大小写）
        if (ext == null || !flag.contains(ext.toLowerCase())) {
            throw new BusinessException(BusErrorCode.FILE_EXT_FAILED);
        }
        return ext;
    }

    // genFileName 方法保持不变，或者根据需要优化
    private String genFileName(String userId, String ext, String folder) {
        return folder + "/" + userId + "_" + DateUtil.format(new Date(), "yyyyMM") + RandomUtil.randomString(4) + "." + ext;
    }


}
