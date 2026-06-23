package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.Tenant;
import com.jnysx.aics.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/tenant")
@RequiredArgsConstructor
@RequireLogin
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(Tenant::getTenantName, keyword)
                .or().like(Tenant::getTenantCode, keyword)
                .or().like(Tenant::getContactName, keyword)
            );
        }
        if (status != null) {
            wrapper.eq(Tenant::getStatus, status);
        }
        wrapper.orderByDesc(Tenant::getCreateTime);
        return Result.ok(tenantService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) return Result.fail("租户不存在");
        return Result.ok(tenant);
    }

    @PostMapping
    public Result<?> create(@RequestBody Tenant tenant) {
        if (tenant.getTenantCode() == null || tenant.getTenantCode().isEmpty()) {
            tenant.setTenantCode("T" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        }
        tenant.setCreateTime(LocalDateTime.now());
        tenant.setUpdateTime(LocalDateTime.now());
        tenant.setStatus(1);
        tenantService.save(tenant);
        return Result.ok("租户创建成功", tenant);
    }

    @PutMapping
    public Result<?> update(@RequestBody Tenant tenant) {
        tenant.setUpdateTime(LocalDateTime.now());
        tenantService.updateById(tenant);
        return Result.ok("租户更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        tenantService.removeById(id);
        return Result.ok("租户删除成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) return Result.fail("租户不存在");
        tenant.setStatus(status);
        tenant.setUpdateTime(LocalDateTime.now());
        tenantService.updateById(tenant);
        return Result.ok("状态更新成功");
    }
}
