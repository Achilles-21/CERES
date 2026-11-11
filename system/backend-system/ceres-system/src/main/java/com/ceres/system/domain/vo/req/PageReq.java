package com.ceres.system.domain.vo.req;

import lombok.Data;

import java.util.List;

@Data
public class PageReq {

    private Long pageNum;

    private Long pageSize;
}
