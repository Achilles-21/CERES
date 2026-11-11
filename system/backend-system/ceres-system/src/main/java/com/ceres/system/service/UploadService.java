package com.ceres.system.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    String uploading(MultipartFile file, String folderName);

}
