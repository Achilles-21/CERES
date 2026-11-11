package com.ceres.common.utils;

import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class VodUtils {

    @Value("${vod.secretId}")
    private String secretId;

    @Value("${vod.secretKey}")
    private String secretKey;

    @Value("${vod.region}")
    private String region;

    private VodUploadClient vodUploadClient;


    @PostConstruct
    private void initVodClient() {
        try {
            vodUploadClient = new VodUploadClient(secretId, secretKey);
            log.info("腾讯云点播客户端初始化成功");
        } catch (Exception e) {
            log.error("点播客户端初始化失败", e);
            throw new RuntimeException("VOD client initialization failed", e);
        }
    }


    public VodUploadResponse upload(MultipartFile file) {
        VodUploadResponse response = null;
        try {
            String path = getTemporaryFilePath(file);
            VodUploadRequest request = new VodUploadRequest();
            request.setMediaFilePath(path);
            response = vodUploadClient.upload(region, request, 60 * 60 * 1000);
        } catch (Exception e) {
            log.error("Upload Err", e);
        }
        return response;
    }

    private String getTemporaryFilePath(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload_", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);

        String tempFilePath = tempFile.getAbsolutePath();

        return tempFilePath;
    }
}
