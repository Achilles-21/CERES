package com.ceres.system.domain.vo.req;

import com.ceres.system.domain.ExamCate;

import lombok.Data;

import java.util.List;

@Data
public class ExamCateReq extends ExamCate {
    private List<Integer> queIds;
}
