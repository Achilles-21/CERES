package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
public class FileInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String createBy;
    private Date createTime;
    private String delFlag;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Date updateTime;

    private String path;

    private Long size;
    private String cate;

}
