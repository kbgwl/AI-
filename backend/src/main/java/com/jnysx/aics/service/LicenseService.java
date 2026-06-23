package com.jnysx.aics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jnysx.aics.entity.License;

import java.util.Map;

public interface LicenseService extends IService<License> {
    String getMachineCode();
    String generateLicenseKey(String customerName, String planType, int durationDays, int maxAgents);
    Map<String, Object> validateLicense();
    boolean activateLicense(String licenseKey, String machineCode);
    Map<String, Object> getLicenseInfo();
}
