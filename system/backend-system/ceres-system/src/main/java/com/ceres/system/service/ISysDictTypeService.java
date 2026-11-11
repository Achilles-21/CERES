package com.ceres.system.service;

import com.ceres.common.core.domain.entity.SysDictData;
import com.ceres.common.core.domain.entity.SysDictType;

import java.util.List;

public interface ISysDictTypeService
{
    public List<SysDictType> selectDictTypeList(SysDictType dictType);

    public List<SysDictType> selectDictTypeAll();

    public List<SysDictData> selectDictDataByType(String dictType);

    public SysDictType selectDictTypeById(Long dictId);

    public SysDictType selectDictTypeByType(String dictType);

    public void deleteDictTypeByIds(Long[] dictIds);

    public void loadingDictCache();

    public void clearDictCache();

    public void resetDictCache();

    public int insertDictType(SysDictType dictType);

    public int updateDictType(SysDictType dictType);

    public boolean checkDictTypeUnique(SysDictType dictType);
}
