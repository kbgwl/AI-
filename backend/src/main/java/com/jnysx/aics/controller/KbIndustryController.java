package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.KbIndustry;
import com.jnysx.aics.service.KbIndustryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 行业管理控制器
 * @author 空白格AI
 */
@Slf4j
@RestController
@RequestMapping("/api/industry")
@RequiredArgsConstructor
@RequireLogin
public class KbIndustryController {
    
    private final KbIndustryService industryService;
    
    @GetMapping("/list")
    public Result<Page<KbIndustry>> listIndustries(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer status) {
        
        Page<KbIndustry> page = new Page<>(current, size);
        LambdaQueryWrapper<KbIndustry> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(KbIndustry::getIndustryName, keyword)
                    .or().like(KbIndustry::getIndustryCode, keyword)
                    .or().like(KbIndustry::getDescription, keyword));
        }
        
        wrapper.eq(KbIndustry::getStatus, status)
                .orderByAsc(KbIndustry::getSortOrder);
        
        Page<KbIndustry> result = industryService.page(page, wrapper);
        return Result.ok(result);
    }
    
    @GetMapping("/active")
    public Result<List<KbIndustry>> listActiveIndustries() {
        List<KbIndustry> list = industryService.listActiveIndustries();
        return Result.ok(list);
    }
    
    @GetMapping("/{id}")
    public Result<KbIndustry> getIndustry(@PathVariable Long id) {
        KbIndustry industry = industryService.getById(id);
        if (industry == null) {
            return Result.fail("行业不存在");
        }
        return Result.ok(industry);
    }
    
    @PostMapping
    public Result<KbIndustry> addIndustry(@RequestBody KbIndustry industry) {
        if (industry.getIndustryCode() == null || industry.getIndustryCode().isEmpty()) {
            String code = industry.getIndustryName().substring(0, Math.min(3, industry.getIndustryName().length()))
                    .toLowerCase() + System.currentTimeMillis();
            industry.setIndustryCode(code);
        }
        
        if (industry.getIcon() == null || industry.getIcon().isEmpty()) {
            industry.setIcon("🌐");
        }
        if (industry.getSortOrder() == null) {
            industry.setSortOrder(0);
        }
        if (industry.getStatus() == null) {
            industry.setStatus(1);
        }
        
        industryService.save(industry);
        return Result.ok(industry);
    }
    
    @PutMapping
    public Result<KbIndustry> updateIndustry(@RequestBody KbIndustry industry) {
        if (industry.getId() == null) {
            return Result.fail("ID 不能为空");
        }
        industryService.updateById(industry);
        return Result.ok(industry);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteIndustry(@PathVariable Long id) {
        industryService.removeById(id);
        return Result.ok();
    }
    
    @PostMapping("/import-defaults")
    public Result<Void> importDefaults() {
        if (industryService.count() > 0) {
            return Result.fail("已存在行业数据，无需导入");
        }
        
        String[][] defaults = {
            {"电商零售", "ecommerce", "电商平台、零售商城、品牌官网等", "🛒"},
            {"教育培训", "education", "K12 教育、职业培训、知识付费等", "📚"},
            {"医疗健康", "healthcare", "医院、诊所、健康管理、医药电商等", "🏥"},
            {"金融服务", "finance", "银行、保险、证券、理财等", "💰"},
            {"生活服务", "lifestyle", "餐饮、酒店、旅游、家政等", "🏡"},
            {"企业服务", "enterprise", "SaaS、咨询、法律、人力资源等", "💼"},
            {"科技互联网", "technology", "软件开发、人工智能、云计算等", "💎"},
            {"制造业", "manufacturing", "工业制造、生产加工、供应链等", "🏭"},
            {"文化传媒", "media", "媒体出版、广告营销、娱乐影视等", "🎭"},
            {"其他行业", "other", "其他未分类行业", "🏷️"}
        };
        
        int order = 1;
        for (String[] item : defaults) {
            KbIndustry industry = new KbIndustry();
            industry.setIndustryName(item[0]);
            industry.setIndustryCode(item[1]);
            industry.setDescription(item[2]);
            industry.setIcon(item[3]);
            industry.setSortOrder(order++);
            industry.setStatus(1);
            industryService.save(industry);
        }
        
        return Result.ok();
    }
}
