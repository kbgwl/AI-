package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.KbIndustry;
import com.jnysx.aics.mapper.KbIndustryMapper;
import com.jnysx.aics.service.KbIndustryService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 行业服务实现
 * @author 空白格·AI
 */
@Service
public class KbIndustryServiceImpl extends ServiceImpl<KbIndustryMapper, KbIndustry> implements KbIndustryService {
    
    @Override
    public List<KbIndustry> listActiveIndustries() {
        return this.list(new LambdaQueryWrapper<KbIndustry>()
                .eq(KbIndustry::getStatus, 1)
                .orderByAsc(KbIndustry::getSortOrder));
    }
    
    @Override
    public KbIndustry getByCode(String industryCode) {
        return this.getOne(new LambdaQueryWrapper<KbIndustry>()
                .eq(KbIndustry::getIndustryCode, industryCode));
    }
}
