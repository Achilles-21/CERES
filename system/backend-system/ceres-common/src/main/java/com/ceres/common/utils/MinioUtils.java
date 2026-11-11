package com.ceres.common.utils;

import cn.hutool.core.lang.UUID;
import com.ceres.common.constant.CommonConstants;
import com.ceres.common.utils.file.FileUtils;
import com.ceres.common.utils.uuid.IdUtils;
import io.minio.*;
import io.minio.http.Method;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioUtils {

    private MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.accessSecret}")
    private String accessSecret;

    @Value("${minio.bucketName}")
    private String bucketName;

    @PostConstruct
    private MinioClient client() {
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();
            log.info("MinIO服务器开始连接------");
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, accessSecret)
                    .httpClient(okHttpClient)
                    .build();
            log.info("MinIO连接完成，开始创建默认bucket");
            createBucket();
        } catch (Exception e) {
            log.error("连接MinIO服务器异常：{}", e);
        }
        return minioClient;
    }

    public MinioClient getClient() {
        return minioClient;
    }

    private static final int DEFAULT_EXPIRY_TIME = 7 * 24 * 3600;

    public Boolean bucketExists(String bucketName) {
        Boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            return false;
        }
        return found;
    }

    @SneakyThrows
    public boolean makeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (!flag) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            return true;
        } else {
            return false;
        }
    }

    public String upload(MultipartFile file) {
        String fileName = "";
        try {
            String extName = FileUtils.getSuffix(file.getOriginalFilename());
            fileName = IdUtils.simpleUUID() + CommonConstants.DOT + extName;
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucketName).object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public String upload(MultipartFile file, String folderName) {
        String fileName = "";
        InputStream inputStream = null;
        try {
            String extName = FileUtils.getSuffix(file.getOriginalFilename());
            String uuid = IdUtils.simpleUUID();
            fileName = uuid + CommonConstants.DOT + extName;
            inputStream = file.getInputStream();
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(folderName + CommonConstants.SLASH + fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileName;
    }

    public String uploadBase64Image(InputStream inputStream) {
        String fileName = null;
        String prefix = "chat-model-image" + CommonConstants.SLASH;
        try {
            fileName = UUID.randomUUID() + ".png";
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(prefix + fileName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType("image/png")
                    .build();
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return endpoint + "/" + bucketName + "/" + prefix + fileName;
    }

    public String preview(String fileName) {
        GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder().bucket(bucketName).object(fileName).method(Method.GET).build();
        try {
            String url = minioClient.getPresignedObjectUrl(build);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void createBucket() throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

}
