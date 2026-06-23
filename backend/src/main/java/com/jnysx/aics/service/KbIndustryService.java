package com.jnysx.aics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jnysx.aics.entity.KbIndustry;
import java.util.List;

/**
 * 行业服务接口
 * @author 空白格·AI
 */
public interface KbIndustryService extends IService<KbIndustry> {
    
    /**
     * 获取所有启用的行业列表
     */
    List<KbIndustry> listActiveIndustries();
    
    /**
     * 根据编码获取行业
     */
    KbIndustry getByCode(String industryCode);
}
