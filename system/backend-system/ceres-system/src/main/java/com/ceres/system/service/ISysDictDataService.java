package com.ceres.system.service;

import com.ceres.common.core.domain.entity.SysDictData;

import java.util.List;

public interface ISysDictDataService
{
    public List<SysDictData> selectDictDataList(SysDictData dictData);

    public String selectDictLabel(String dictType, String dictValue);

    public SysDictData selectDictDataById(Long dictCode);

    public void deleteDictDataByIds(Long[] dictCodes);

    public int insertDictData(SysDictData dictData);

    public int updateDictData(SysDictData dictData);
}
