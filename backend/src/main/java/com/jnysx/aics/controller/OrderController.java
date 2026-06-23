package com.jnysx.aics.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.Order;
import com.jnysx.aics.service.OrderService;
import com.jnysx.aics.service.PaymentGatewayService;
import com.jnysx.aics.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@RequireLogin
public class OrderController {

    private final OrderService orderService;
    private final SystemConfigService systemConfigService;
    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/create")
    public Result<?> create(@RequestBody java.util.Map<String, Object> body) {
        Long tenantId = Long.valueOf(body.get("tenantId").toString());
        Long planId = Long.valueOf(body.get("planId").toString());
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 1;
        Order order = orderService.createOrder(tenantId, planId, quantity);
        return Result.ok(order);
    }

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(Order::getTenantId, tenantId);
        if (status != null && !status.isEmpty()) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreateTime);
        return Result.ok(orderService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    @PostMapping("/pay")
    public Result<?> pay(@RequestBody java.util.Map<String, String> body) {
        String orderNo = body.get("orderNo");
        String channel = body.getOrDefault("channel", "alipay");

        String configStr = systemConfigService.getConfigValue("payment." + channel);
        if (configStr == null || configStr.isEmpty()) {
            return Result.fail(400, "未配置" + ("alipay".equals(channel) ? "支付宝" : "微信支付"));
        }
        JSONObject config = JSON.parseObject(configStr);
        if (!Boolean.TRUE.equals(config.getBoolean("enabled"))) {
            return Result.fail(400, "支付渠道未启用");
        }

        String tradeNo = "MOCK" + System.currentTimeMillis();
        boolean ok = orderService.payOrder(orderNo, channel, tradeNo);
        return ok ? Result.ok("支付成功") : Result.fail("支付失败");
    }

    @PutMapping("/{id}/refund")
    public Result<?> refund(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) return Result.fail("订单不存在");
        if (!"paid".equals(order.getStatus())) return Result.fail("只能退款已支付的订单");

        String channel = order.getPayChannel();
        if (channel == null || channel.isEmpty()) {
            return Result.fail(400, "订单无支付渠道信息");
        }

        String configStr = systemConfigService.getConfigValue("payment." + channel);
        if (configStr == null || configStr.isEmpty()) {
            return Result.fail(400, "支付渠道未配置");
        }
        JSONObject config = JSON.parseObject(configStr);
        if (!Boolean.TRUE.equals(config.getBoolean("enabled"))) {
            return Result.fail(400, "支付渠道未启用");
        }

        JSONObject refundResult;
        try {
            if ("alipay".equals(channel)) {
                refundResult = paymentGatewayService.alipayRefund(
                        config.getString("appId"),
                        config.getString("privateKey"),
                        config.getString("publicKey"),
                        order.getPayTradeNo(),
                        order.getOrderNo(),
                        order.getPayAmount().toPlainString(),
                        "用户申请退款"
                );
            } else if ("wechat".equals(channel)) {
                int amountFen = order.getPayAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue();
                refundResult = paymentGatewayService.wechatRefund(
                        config.getString("mchId"),
                        config.getString("apiKey"),
                        config.getString("serialNo"),
                        order.getPayTradeNo(),
                        order.getOrderNo(),
                        amountFen,
                        "用户申请退款"
                );
            } else {
                return Result.fail(400, "不支持的支付渠道：" + channel);
            }
        } catch (Exception e) {
            log.error("Refund error: {}", e.getMessage(), e);
            return Result.fail(500, "退款请求异常：" + e.getMessage());
        }

        if (Boolean.TRUE.equals(refundResult.getBoolean("success"))) {
            order.setStatus("refunded");
            order.setUpdateTime(java.time.LocalDateTime.now());
            orderService.updateById(order);
            return Result.ok("退款成功：" + refundResult.getString("message"));
        } else {
            return Result.fail(400, "退款失败：" + refundResult.getString("message"));
        }
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) return Result.fail("订单不存在");
        if (!"pending".equals(order.getStatus())) return Result.fail("只能取消待支付的订单");
        order.setStatus("cancelled");
        order.setUpdateTime(java.time.LocalDateTime.now());
        orderService.updateById(order);
        return Result.ok("订单已取消");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) return Result.fail("订单不存在");
        if ("paid".equals(order.getStatus())) return Result.fail("已支付订单不能删除");
        orderService.removeById(id);
        return Result.ok("已删除");
    }
}
