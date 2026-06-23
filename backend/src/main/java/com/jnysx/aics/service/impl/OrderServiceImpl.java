package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.Order;
import com.jnysx.aics.entity.Plan;
import com.jnysx.aics.entity.Subscription;
import com.jnysx.aics.mapper.OrderMapper;
import com.jnysx.aics.service.OrderService;
import com.jnysx.aics.service.PlanService;
import com.jnysx.aics.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private PlanService planService;
    @Autowired
    private SubscriptionService subscriptionService;

    private static final DateTimeFormatter ORDER_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    @Override
    public Order createOrder(Long tenantId, Long planId, Integer quantity) {
        Plan plan = planService.getById(planId);
        if (plan == null) throw new RuntimeException("套餐不存在");

        Order order = new Order();
        order.setOrderNo("ORD" + LocalDateTime.now().format(ORDER_FMT) + String.format("%04d", RANDOM.nextInt(10000)));
        order.setTenantId(tenantId);
        order.setPlanId(planId);
        order.setPlanName(plan.getPlanName());
        order.setAmount(BigDecimal.valueOf(plan.getPrice() * quantity));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(order.getAmount().subtract(order.getDiscountAmount()));
        order.setQuantity(quantity);
        order.setDurationDays(plan.getDurationDays() * quantity);
        order.setStatus("pending");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);

        save(order);
        return order;
    }

    @Override
    public boolean payOrder(String orderNo, String channel, String tradeNo) {
        Order order = lambdaQuery().eq(Order::getOrderNo, orderNo).one();
        if (order == null || !"pending".equals(order.getStatus())) return false;

        order.setStatus("paid");
        order.setPayChannel(channel);
        order.setPayTradeNo(tradeNo);
        order.setPayTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusDays(order.getDurationDays()));
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        subscriptionService.activateSubscription(order.getTenantId(), order.getPlanId(), order.getId());
        return true;
    }
}
