package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.License;
import com.jnysx.aics.mapper.LicenseMapper;
import com.jnysx.aics.service.LicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 授权许可证服务实现
 * 核心防倒卖逻辑：机器码绑定 + 激活码验证 + 到期控制
 */
@Service
public class LicenseServiceImpl extends ServiceImpl<LicenseMapper, License> implements LicenseService {

    private static final Logger log = LoggerFactory.getLogger(LicenseServiceImpl.class);
    private static final String LICENSE_SECRET = "AiCs2026!License@Secret#Key";
    private static final String ACTIVATION_PREFIX = "AICS-";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemConfigServiceImpl systemConfigService;

    @PostConstruct
    public void initTable() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS `tb_license` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`license_key` VARCHAR(100) NOT NULL COMMENT '许可证密钥'," +
                "`machine_code` VARCHAR(200) COMMENT '机器码'," +
                "`customer_name` VARCHAR(100) COMMENT '客户名称'," +
                "`customer_contact` VARCHAR(100) COMMENT '客户联系方式'," +
                "`plan_type` VARCHAR(20) DEFAULT 'basic' COMMENT '套餐：trial/basic/pro/enterprise'," +
                "`max_agents` INT DEFAULT 5 COMMENT '最大坐席数'," +
                "`max_sessions` INT DEFAULT 100 COMMENT '最大会话数'," +
                "`license_type` VARCHAR(20) DEFAULT 'time' COMMENT '许可类型：time-lifetime'," +
                "`expire_time` DATETIME COMMENT '到期时间'," +
                "`activate_time` DATETIME COMMENT '激活时间'," +
                "`status` TINYINT DEFAULT 0 COMMENT '状态：0未激活 1已激活 2已过期 3已禁用'," +
                "`remark` VARCHAR(500) COMMENT '备注'," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "`deleted` TINYINT DEFAULT 0," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_license_key` (`license_key`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='软件授权许可证表'"
            );
            log.info("License table initialized");
        } catch (Exception e) {
            log.error("Failed to init license table: {}", e.getMessage());
        }
    }

    @Override
    public String getMachineCode() {
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface ni = nets.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                    }
                    break;
                }
            }
            if (sb.length() == 0) {
                sb.append("DEFAULTMACHINE");
            }
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 32).toUpperCase();
        } catch (Exception e) {
            log.error("Failed to generate machine code: {}", e.getMessage());
            return "ERROR-" + System.currentTimeMillis();
        }
    }

    @Override
    public String generateLicenseKey(String customerName, String planType, int durationDays, int maxAgents) {
        try {
            String raw = customerName + "|" + planType + "|" + durationDays + "|" + maxAgents + "|" + System.currentTimeMillis();
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(LICENSE_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(ACTIVATION_PREFIX);
            for (int i = 0; i < hash.length; i += 2) {
                sb.append(String.format("%02X", hash[i] & 0xFF));
                if (sb.length() > 30) break;
            }
            return sb.toString();
        } catch (Exception e) {
            return ACTIVATION_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
        }
    }

    @Override
    public boolean activateLicense(String licenseKey, String machineCode) {
        LambdaQueryWrapper<License> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(License::getLicenseKey, licenseKey);
        License license = getOne(wrapper);

        if (license == null) return false;
        if (license.getStatus() == 3) return false;

        if (license.getMachineCode() != null && !license.getMachineCode().isEmpty()
                && !license.getMachineCode().equals(machineCode)) {
            return false;
        }

        license.setMachineCode(machineCode);
        license.setActivateTime(LocalDateTime.now());
        license.setStatus(1);

        if ("trial".equals(license.getPlanType())) {
            license.setExpireTime(LocalDateTime.now().plusDays(7));
        } else if ("basic".equals(license.getPlanType())) {
            license.setExpireTime(LocalDateTime.now().plusDays(365));
        } else if ("lifetime".equals(license.getLicenseType())) {
            license.setExpireTime(LocalDateTime.of(2099, 12, 31, 23, 59, 59));
        }

        boolean result = updateById(license);

        if (result) {
            systemConfigService.setConfigValue("license.status", "active", "License Status", "授权状态");
            systemConfigService.setConfigValue("license.key", licenseKey, "License Key", "授权密钥");
            systemConfigService.setConfigValue("license.plan", license.getPlanType(), "License Plan", "授权套餐");
            systemConfigService.setConfigValue("license.expire", license.getExpireTime() != null ? license.getExpireTime().toString() : "", "License Expire", "授权到期时间");
            systemConfigService.setConfigValue("license.agents", String.valueOf(license.getMaxAgents()), "Max Agents", "最大坐席数");
        }

        return result;
    }

    @Override
    public Map<String, Object> validateLicense() {
        Map<String, Object> result = new HashMap<>();
        String licenseKey = systemConfigService.getConfigValue("license.key");
        if (licenseKey == null || licenseKey.isEmpty()) {
            result.put("valid", false);
            result.put("message", "未授权");
            result.put("status", "unlicensed");
            return result;
        }

        LambdaQueryWrapper<License> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(License::getLicenseKey, licenseKey);
        License license = getOne(wrapper);

        if (license == null) {
            result.put("valid", false);
            result.put("message", "授权无效");
            result.put("status", "invalid");
            return result;
        }

        if (license.getStatus() == 3) {
            result.put("valid", false);
            result.put("message", "授权已被禁用");
            result.put("status", "disabled");
            return result;
        }

        if (license.getStatus() == 0) {
            result.put("valid", false);
            result.put("message", "授权未激活");
            result.put("status", "inactive");
            return result;
        }

        if (license.getExpireTime() != null && license.getExpireTime().isBefore(LocalDateTime.now())) {
            license.setStatus(2);
            updateById(license);
            result.put("valid", false);
            result.put("message", "授权已过期");
            result.put("status", "expired");
            return result;
        }

        String currentMachineCode = getMachineCode();
        if (license.getMachineCode() != null && !license.getMachineCode().equals(currentMachineCode)) {
            result.put("valid", false);
            result.put("message", "授权与当前服务器不匹配");
            result.put("status", "mismatch");
            return result;
        }

        result.put("valid", true);
        result.put("message", "授权有效");
        result.put("status", "active");
        result.put("plan", license.getPlanType());
        result.put("expireTime", license.getExpireTime());
        result.put("maxAgents", license.getMaxAgents());
        result.put("maxSessions", license.getMaxSessions());
        result.put("customerName", license.getCustomerName());
        return result;
    }

    @Override
    public Map<String, Object> getLicenseInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("machineCode", getMachineCode());

        String licenseKey = systemConfigService.getConfigValue("license.key");
        if (licenseKey != null && !licenseKey.isEmpty()) {
            LambdaQueryWrapper<License> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(License::getLicenseKey, licenseKey);
            License license = getOne(wrapper);
            if (license != null) {
                info.put("licenseKey", license.getLicenseKey());
                info.put("planType", license.getPlanType());
                info.put("status", license.getStatus());
                info.put("expireTime", license.getExpireTime());
                info.put("activateTime", license.getActivateTime());
                info.put("customerName", license.getCustomerName());
                info.put("maxAgents", license.getMaxAgents());
                info.put("maxSessions", license.getMaxSessions());
            }
        }
        return info;
    }
}
