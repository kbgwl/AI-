package com.jnysx.aics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jnysx.aics.entity.Subscription;

public interface SubscriptionService extends IService<Subscription> {
    void activateSubscription(Long tenantId, Long planId, Long orderId);
}
