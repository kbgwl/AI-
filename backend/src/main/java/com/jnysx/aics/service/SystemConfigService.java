package com.jnysx.aics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jnysx.aics.entity.SystemConfig;

/**
 * 系统配置Service接口
 */
public interface SystemConfigService extends IService<SystemConfig> {
    /**
     * 获取配置值（按configKey）
     */
    String getConfigValue(String configKey);

    /**
     * 设置配置值（存在则更新，不存在则新增）
     */
    boolean setConfigValue(String configKey, String configValue, String configName, String description);
}
