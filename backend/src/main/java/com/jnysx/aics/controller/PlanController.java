package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.Plan;
import com.jnysx.aics.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/plan")
@RequiredArgsConstructor
@RequireLogin
public class PlanController {

    private final PlanService planService;

    @GetMapping("/list")
    public Result<?> list() {
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Plan::getSort);
        return Result.ok(planService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        Plan plan = planService.getById(id);
        if (plan == null) return Result.fail("套餐不存在");
        return Result.ok(plan);
    }

    @PostMapping
    public Result<?> create(@RequestBody Plan plan) {
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        plan.setStatus(1);
        planService.save(plan);
        return Result.ok("套餐创建成功", plan);
    }

    @PutMapping
    public Result<?> update(@RequestBody Plan plan) {
        plan.setUpdateTime(LocalDateTime.now());
        planService.updateById(plan);
        return Result.ok("套餐更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        planService.removeById(id);
        return Result.ok("套餐删除成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Plan plan = planService.getById(id);
        if (plan == null) return Result.fail("套餐不存在");
        plan.setStatus(status);
        plan.setUpdateTime(LocalDateTime.now());
        planService.updateById(plan);
        return Result.ok("状态更新成功");
    }
}
