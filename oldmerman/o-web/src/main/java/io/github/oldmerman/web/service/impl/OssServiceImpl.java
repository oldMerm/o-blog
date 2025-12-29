package io.github.oldmerman.web.service.impl;

import cn.hutool.core.date.DateUtil;
import com.aliyun.oss.OSS;
import io.github.oldmerman.common.enums.BusErrorCode;
import io.github.oldmerman.common.enums.NumEnum;
import io.github.oldmerman.common.enums.WebEnum;
import io.github.oldmerman.common.exception.BusinessException;
import io.github.oldmerman.model.po.User;
import io.github.oldmerman.web.mapper.UserMapper;
import io.github.oldmerman.web.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OssServiceImpl implements OssService {

    private final UserMapper userMapper;

    private final OSS ossClient;

    private static final String BUCKET = "project-oldmerman";

    // 定义允许的格式常量，方便维护
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

    /**
     * 后端转存用户头像，限定格式
     * @param userId 用户id
     * @param file   文件
     * @return 唯一key
     */
    @Transactional
    public String uploadUsrImage(Long userId, MultipartFile file) {
        // 校验文件扩展名
        String originalFilename = Optional.of(file.getOriginalFilename())
                .orElseThrow(() -> new BusinessException(BusErrorCode.FILE_EXT_FAILED));

        // 获取扩展名（更健壮的方式）
        String ext = StringUtils.getFilenameExtension(originalFilename);

        // 校验格式（忽略大小写）
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BusinessException(BusErrorCode.FILE_EXT_FAILED);
        }

        // 准备新文件Key和旧文件Key
        String newFileKey = genFileName(userId, ext, WebEnum.USER_PREFIX.getValue());
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
    public String genPreviewUrl(String key) {
        Date expires = new Date(System.currentTimeMillis() + NumEnum.USER_ATTR_EXPIRE.getValue()*1000);
        if(key == null){
            return null;
        }
        URL url = ossClient.generatePresignedUrl(BUCKET, key, expires);
        return url.toString();
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
    private String genFileName(Long userId, String ext, String folder){
        return folder + "/" + userId + "-" + DateUtil.format(new Date(), "yyyyMM") + "." + ext;
    }
}
