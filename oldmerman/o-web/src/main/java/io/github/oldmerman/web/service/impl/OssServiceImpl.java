package io.github.oldmerman.web.service.impl;

import cn.hutool.core.date.DateUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.VoidResult;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.enums.NumEnum;
import io.github.oldmerman.common.enums.WebEnum;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.model.po.User;
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

    private static final String BUCKET = "project-oldmerman";

    // 定义允许的格式常量，方便维护
    private static final List<String> IMG_ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private static final List<String> MD_ALLOWED_EXTENSIONS = List.of("md");

    /**
     * 后端转存用户头像，限定格式
     * @param userId 用户id
     * @param file 文件
     * @return 唯一key
     */
    @Transactional
    public String uploadUsrImage(Long userId, MultipartFile file) {
        // 校验文件扩展名
        String ext = getAllowExt(file, IMG_ALLOWED_EXTENSIONS);

        // 准备新文件Key和旧文件Key
        String newFileKey = genFileName(userId.toString(), ext, WebEnum.USER_PREFIX.getValue());
        String oldFileKey = Optional.ofNullable(userMapper.selectUserById(userId))
                .map(User::getAttr)
                .orElse(null);

        try {
            // 上传到 OSS
            ossClient.putObject(BUCKET, newFileKey, file.getInputStream());

            // 更新数据库 (建议加上事务注解 @Transactional 在 Service 层)
            userMapper.updateUserAttrKey(userId, newFileKey);

            // 异步或尝试删除旧图片（非核心业务，即使失败也不应影响主流程）
            deleteOldImageSilently(oldFileKey);

            return newFileKey;

        } catch (IOException | com.aliyun.oss.OSSException e) {
            // 上传或流读取失败，记录日志
            log.error("用户:{} 头像上传失败: {}", userId, e.getMessage(), e);
            throw new BusinessException(BusErrorCode.UPLOAD_FAILED);
        }
    }

    /**
     * 生成闲时预览的URL
     * @return url
     */
    public String genPreviewURL(String key) {
        Date expires = new Date(System.currentTimeMillis() + NumEnum.USER_ATTR_EXPIRE.getValue()*1000);
        if(!StringUtils.hasText(key)){
            return null;
        }
        URL url = ossClient.generatePresignedUrl(BUCKET, key, expires);
        return url.toString();
    }

    /**
     * 生成公共url，只生产一次
     * @param keys 文件路径
     * @param bucket 所属bucket
     * @return 处理后可直接访问的URL
     */
    public List<String> genPublicURL(List<String> keys, String bucket){
        if(keys.isEmpty()){
            throw new BusinessException(BusErrorCode.UPLOAD_FAILED);
        }
        return keys.stream().map(key -> "https://" + bucket + ".oss-cn-guangzhou.aliyuncs.com/" + key).toList();
    }

    /**
     * 通用方法，批量上传图片
     * @param files 图片集合
     * @return 图片key集合
     */
    public List<String> uploadBatch(Long id, List<String> path,
                                    List<MultipartFile> files,String bucket) {
        List<String> batchList = new ArrayList<>();
        int size, i;
        if((size = path.size()) != files.size()){
            throw new BusinessException(BusErrorCode.FILE_LEN_FAILED);
        }
        try {
            for (i = 0; i < size; i++) {
                MultipartFile file = files.get(i);
                // 校验文件扩展名
                String ext = getAllowExt(file, IMG_ALLOWED_EXTENSIONS);
                String key = genFileName(path.get(i), ext, WebEnum.MD_IMG_PREFIX.getValue());
                if(key.contains("/./")){
                    key = Paths.get("", key.split("/")).normalize().toString().replace("\\","/");
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

    /**
     * 上传md文档
     * @return 唯一key
     */
    public String uploadMd(Long id, MultipartFile file){
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

    /**
     * @param key 需删除key
     * @param bucket 所属bucket
     */
    public void deleteOne(String key, String bucket) {
        bucket = bucket == null ? BUCKET : bucket;
        try {
            ossClient.deleteObject(bucket, key);
        } catch (Exception e) {
            log.warn("删除Key失败: {}, 原因: {}", key, e.getMessage());
        }
    }

    /**
     * 批量删除key
     * @param keys key集合
     * @param bucket 所属bucket
     */
    public void deleteBatch(List<String> keys, String bucket){
        bucket = bucket == null ? BUCKET : bucket;
        DeleteObjectsRequest request = new DeleteObjectsRequest(bucket);
        request.setKeys(keys);
        request.setQuiet(true);
        DeleteObjectsResult result = ossClient.deleteObjects(request);
        List<String> deletedObjects = result.getDeletedObjects();
        if(!deletedObjects.isEmpty()){
            for (String key : deletedObjects) {
                log.warn("文件删除失败,key:{}",key);
            }
        }
    }

    /**
     * 安全获取对应文件拓展名
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

    /**
     * 辅助方法：静默删除旧图片
     * 删除旧图属于清理操作，不应因为网络抖动导致用户修改头像失败
     */
    private void deleteOldImageSilently(String key) {
        if (StringUtils.hasText(key)) {
            try {
                ossClient.deleteObject(BUCKET, key);
            } catch (Exception e) {
                log.warn("旧头像清理失败，Key: {}, 原因: {}", key, e.getMessage());
            }
        }
    }

    // genFileName 方法保持不变，或者根据需要优化
    private String genFileName(String userId, String ext, String folder){
        return folder + "/" + userId + "_" + DateUtil.format(new Date(), "yyyyMM") + "." + ext;
    }


}
