package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.Plan;
import com.jnysx.aics.entity.Subscription;
import com.jnysx.aics.mapper.SubscriptionMapper;
import com.jnysx.aics.service.PlanService;
import com.jnysx.aics.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, Subscription> implements SubscriptionService {

    @Autowired
    private PlanService planService;

    @Override
    public void activateSubscription(Long tenantId, Long planId, Long orderId) {
        Plan plan = planService.getById(planId);
        if (plan == null) return;

        Subscription old = lambdaQuery()
                .eq(Subscription::getTenantId, tenantId)
                .eq(Subscription::getStatus, "active")
                .one();
        if (old != null) {
            old.setStatus("replaced");
            updateById(old);
        }

        Subscription sub = new Subscription();
        sub.setTenantId(tenantId);
        sub.setPlanId(planId);
        sub.setPlanCode(plan.getPlanCode());
        sub.setPlanName(plan.getPlanName());
        sub.setOrderId(orderId);
        sub.setMaxAgents(plan.getMaxAgents());
        sub.setMaxSessions(plan.getMaxSessions());
        sub.setMaxKnowledgeItems(plan.getMaxKnowledgeItems());
        sub.setStatus("active");
        sub.setStartDate(LocalDateTime.now());
        sub.setExpireDate(LocalDateTime.now().plusDays(plan.getDurationDays()));
        sub.setAutoRenew(false);
        sub.setCreateTime(LocalDateTime.now());
        sub.setUpdateTime(LocalDateTime.now());

        save(sub);
    }
}
