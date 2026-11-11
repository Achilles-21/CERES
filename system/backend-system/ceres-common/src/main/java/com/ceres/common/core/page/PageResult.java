package com.ceres.common.core.page;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T, F> implements Serializable{

    private Integer total;

    private List<T> row;

    private Integer pageNum;

    private Integer pageSize;

    public PageResult(List<T> row,List<F> objects) {
        this.total = (int) PageInfo.of(objects).getTotal();
        this.row = row;
    }

    public PageResult(List<T> row, long total, int pageNum, int pageSize) {
        this.total = (int) total;
        this.row = row;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

}
