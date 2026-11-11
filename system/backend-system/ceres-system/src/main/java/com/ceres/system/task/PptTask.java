package com.ceres.system.task;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ceres.common.utils.MinioUtils;
import com.ceres.system.domain.ResourceVideo;
import com.ceres.system.mapper.ResourceVideoMapper;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PptTask {

    @Resource
    private MinioUtils minioUtils;
    @Resource
    private ResourceVideoMapper resourceVideoMapper;
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.bucketName}")
    private String bucketName;

    @Scheduled(cron = "0 0 0 1/15 * ?")
    public void UpdatePptName(){
        LambdaQueryWrapper<ResourceVideo> lambdaQueryWrapper = new LambdaQueryWrapper<ResourceVideo>().eq(ResourceVideo::getDelFlag, "0");
        List<ResourceVideo> resourceVideos = resourceVideoMapper.selectList(lambdaQueryWrapper);
        List<ResourceVideo> resourceVideoList = new ArrayList<>();
        for (ResourceVideo resourceVideo : resourceVideos) {
            if(StrUtil.isNotBlank(resourceVideo.getPptUrl())){
                String[] strings = resourceVideo.getPptUrl().split("/"+bucketName+"/");
                String oldPptName = strings[1];
                String newPptName = "ppt/"+System.currentTimeMillis()+resourceVideo.getId().toString()+".pptx";
                ResourceVideo myResourceVideo = new ResourceVideo();
                myResourceVideo.setId(resourceVideo.getId());
                try {
                    MinioClient minioClient = minioUtils.getClient();
                    CopyObjectArgs copyObjectArgs = CopyObjectArgs.builder()
                            .source(CopySource.builder()
                                    .bucket(bucketName).object(oldPptName).build())
                            .bucket(bucketName).object(newPptName)
                            .build();
                    minioClient.copyObject(copyObjectArgs);
                    minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(oldPptName).build());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                myResourceVideo.setPptUrl(endpoint+"/"+bucketName+"/"+newPptName);
                resourceVideoList.add(myResourceVideo);
            }
        }
        resourceVideoMapper.updateBatchPptUrl(resourceVideoList);
    }

}
