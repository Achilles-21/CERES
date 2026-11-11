package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.system.service.UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Resource
    private UploadService uploadService;

    @Log(title = "上传图片")
    @PostMapping("/image")
    public String uploading(@RequestParam("file") MultipartFile file, @RequestParam("folderName") String folderName) throws Exception {
        return uploadService.uploading(file, folderName);
    }

}
