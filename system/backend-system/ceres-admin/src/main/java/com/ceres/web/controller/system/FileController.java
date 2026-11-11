package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.service.IFileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private IFileService fileService;

    @Log(title = "上传文件")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, @RequestParam("folderName") String folderName) {
        return AjaxResult.success(fileService.uploadStaticFile(file, folderName));
    }

    @Log(title = "云点播上传视频")
    @PostMapping("/upload/video")
    public AjaxResult uploadVideo(@RequestParam("file") MultipartFile file, @RequestParam("folderName") String folderName) {
        return AjaxResult.success(fileService.uploadStaticVideo(file, folderName));
    }

}
