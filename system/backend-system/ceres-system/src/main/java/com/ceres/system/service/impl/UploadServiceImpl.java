package com.ceres.system.service.impl;

import com.ceres.common.constant.CommonConstants;
import com.ceres.common.exception.ServiceException;
import com.ceres.common.utils.MinioUtils;
import com.ceres.system.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@Service
@Slf4j
public class UploadServiceImpl implements UploadService {

    @Resource
    private MinioUtils minioUtils;
    @Value("${minio.trueEndpoint}")
    private String trueEndpoint;
    @Value("${minio.bucketName}")
    private String bucket;

    @Override
    public String uploading(MultipartFile file, String folderName) {

        String fileName = "";
        try {
            fileName = minioUtils.upload(file, folderName);
        } catch (Exception e) {
            log.error("文件上传失败，失败原因：{}", e);
            throw new ServiceException("文件上传失败");
        } finally {
            try {
                IOUtils.closeQuietly(file.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String url = trueEndpoint + CommonConstants.SLASH + bucket + CommonConstants.SLASH + folderName + CommonConstants.SLASH + fileName;
        return url;
    }

}
