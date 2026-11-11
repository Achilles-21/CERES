package com.ceres.system.mapper;

import com.ceres.system.domain.SysConfig;

import java.util.List;

public interface SysConfigMapper
{
    public SysConfig selectConfig(SysConfig config);

    public SysConfig selectConfigById(Long configId);

    public List<SysConfig> selectConfigList(SysConfig config);

    public SysConfig checkConfigKeyUnique(String configKey);

    public int insertConfig(SysConfig config);

    public int updateConfig(SysConfig config);

    public int deleteConfigById(Long configId);

    public int deleteConfigByIds(Long[] configIds);
}
