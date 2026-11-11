package com.ceres.system.service;

import com.ceres.system.domain.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface IFileService {


    FileInfo uploadStaticFile(MultipartFile file, String folderName);

    FileInfo uploadStaticVideo(MultipartFile file, String folderName);

    FileInfo uploadFile(MultipartFile file);

    List<Long> uploadFiles(List<MultipartFile> files, String path);

    void download(HttpServletResponse response, byte[] bytes, String fileName);

    void downloadById(Long id, HttpServletResponse response);

    FileInfo getById(Long fileId);

}
