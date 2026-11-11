package com.ceres.system.service;

import com.ceres.system.domain.SysConfig;

import java.util.List;

public interface ISysConfigService
{
    public SysConfig selectConfigById(Long configId);

    public String selectConfigByKey(String configKey);

    public boolean selectCaptchaEnabled();

    public List<SysConfig> selectConfigList(SysConfig config);

    public int insertConfig(SysConfig config);

    public int updateConfig(SysConfig config);

    public void deleteConfigByIds(Long[] configIds);

    public void loadingConfigCache();

    public void clearConfigCache();

    public void resetConfigCache();

    public boolean checkConfigKeyUnique(SysConfig config);
}
