package com.ceres.system.service.impl;

import com.ceres.common.constant.CommonConstants;
import com.ceres.common.exception.ServiceException;
import com.ceres.common.utils.MinioUtils;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.common.utils.VodUtils;
import com.ceres.system.domain.FileInfo;
import com.ceres.system.mapper.FileInfoMapper;
import com.ceres.system.service.IFileService;
import com.qcloud.vod.model.VodUploadResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class FileServiceImpl implements IFileService {

    @Resource
    private FileInfoMapper fileInfoMapper;

    @Resource
    private MinioUtils fileUtils;

    @Resource
    private VodUtils vodUtils;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.trueEndpoint}")
    private String trueEndpoint;


    @Value("${minio.bucketName}")
    private String bucket;

    @Override
    public FileInfo uploadStaticFile(MultipartFile file, String folderName) {
        return upload(file, folderName);
    }

    @Override
    public FileInfo uploadFile(MultipartFile file) {
        return null;
    }

    @Override
    public List<Long> uploadFiles(List<MultipartFile> files, String path) {
        return null;
    }

    @Override
    public void download(HttpServletResponse response, byte[] bytes, String fileName) {
    }

    @Override
    public void downloadById(Long id, HttpServletResponse response) {

    }

    private FileInfo upload(MultipartFile file, String folderName) {
        String fileName = "";
        try {
            fileName = fileUtils.upload(file, folderName);
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
        return record(url, fileName, file, "0");
    }

    @Override
    public FileInfo uploadStaticVideo(MultipartFile file, String folderName) {

        VodUploadResponse response = null;
        try {
            response = vodUtils.upload(file);
        } catch (Exception e) {
            log.error("文件上传失败，失败原因：{}", e);
            throw new ServiceException("文件上传失败");
        }
        return record(response.getMediaUrl(), response.getFileId(), file, "1");
    }

    private FileInfo record(String url, String fileName, MultipartFile file, String cate) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(fileName);
        fileInfo.setFileUrl(url);
        fileInfo.setFileType(file.getContentType());
        fileInfo.setSize(file.getSize());
        fileInfo.setCreateBy(SecurityUtils.getUsername());
        fileInfo.setCate(cate);
        fileInfoMapper.insert(fileInfo);
        return fileInfo;
    }

    @Override
    public FileInfo getById(Long fileId) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        return fileInfo;
    }

}
