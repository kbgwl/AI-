package com.jnysx.aics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jnysx.aics.entity.Order;

public interface OrderService extends IService<Order> {
    Order createOrder(Long tenantId, Long planId, Integer quantity);
    boolean payOrder(String orderNo, String channel, String tradeNo);
}
