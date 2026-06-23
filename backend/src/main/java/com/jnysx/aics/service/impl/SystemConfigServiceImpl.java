package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.SystemConfig;
import com.jnysx.aics.mapper.SystemConfigMapper;
import com.jnysx.aics.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * 系统配置Service实现类
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initTable() {
        try {
            // 检查表是否存在
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS `tb_system_config` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`config_key` VARCHAR(100) NOT NULL COMMENT '配置键'," +
                "`config_value` TEXT COMMENT '配置值'," +
                "`config_name` VARCHAR(100) COMMENT '配置名称'," +
                "`description` VARCHAR(500) COMMENT '描述'," +
                "`status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用'," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_config_key` (`config_key`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表'"
            );
            log.info("System config table initialized");

            // 检查是否有 API Key，如果没有则从环境变量或配置文件初始化
            String existingKey = getConfigValue("deepseek.api-key");
            if (existingKey == null || existingKey.trim().isEmpty()) {
                String envKey = System.getenv("DEEPSEEK_API_KEY");
                if (envKey != null && !envKey.trim().isEmpty()) {
                    setConfigValue("deepseek.api-key", envKey, "DeepSeek API Key", "DeepSeek API 密钥");
                    log.info("Initialized API key from environment variable");
                } else {
                    // 使用默认的 API Key
                    setConfigValue("deepseek.api-key", "YOUR_API_KEY_HERE", "DeepSeek API Key", "DeepSeek API 密钥");
                    log.info("Initialized API key with default value");
                }
            }
        } catch (Exception e) {
            log.error("Failed to init system config table: {}", e.getMessage());
        }
    }

    @Override
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        wrapper.last("LIMIT 1");
        SystemConfig config = getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public boolean setConfigValue(String configKey, String configValue, String configName, String description) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig existing = getOne(wrapper);

        if (existing != null) {
            existing.setConfigValue(configValue);
            if (configName != null) existing.setConfigName(configName);
            if (description != null) existing.setDescription(description);
            existing.setUpdateTime(LocalDateTime.now());
            return updateById(existing);
        } else {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setConfigName(configName);
            config.setDescription(description);
            config.setStatus(1);
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            return save(config);
        }
    }
}
