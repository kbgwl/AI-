package com.jnysx.aics.controller;

import com.jnysx.aics.common.Result;
import com.jnysx.aics.service.WechatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 微信渠道控制器
 * 处理微信公众号/小程序的消息回调
 * @author 济南空白格网络科技有限公司
 */
@RestController
@RequestMapping("/api/wechat")
@RequiredArgsConstructor
public class WechatController {

    private final WechatService wechatService;

    /**
     * 微信服务器验证 GET /api/wechat/callback
     */
    @GetMapping("/callback")
    public String verifyWechat(
            @RequestParam String signature,
            @RequestParam String timestamp,
            @RequestParam String nonce,
            @RequestParam String echostr) {
        String result = wechatService.verify(signature, timestamp, nonce, echostr);
        return result.isEmpty() ? "error" : result;
    }

    /**
     * 接收微信消息 POST /api/wechat/callback
     */
    @PostMapping("/callback")
    public String receiveWechatMessage(@RequestBody String requestBody) {
        try {
            String response = wechatService.processMessage(requestBody);
            if (response.isEmpty()) {
                return "success";
            }
            return response;
        } catch (Exception e) {
            System.out.println("处理微信消息异常：" + e.getMessage());
            return "success";
        }
    }

    /**
     * 获取微信配置信息
     */
    @GetMapping("/config")
    public Result<?> getWechatConfig() {
        return Result.ok(null);
    }
}