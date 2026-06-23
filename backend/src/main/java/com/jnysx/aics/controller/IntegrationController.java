package com.jnysx.aics.controller;

import com.jnysx.aics.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 接入文档Controller
 */
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    /**
     * 获取接入配置信息
     */
    @GetMapping("/config")
    public Result<?> getConfig(@RequestParam(defaultValue = "web") String channel) {
        Map<String, Object> config = new HashMap<>();
        config.put("serverUrl", "http://localhost:8082");
        config.put("wsUrl", "ws://localhost:8082/ws/chat");
        config.put("channel", channel);
        
        // 嵌入代码
        config.put("iframeCode", generateIframeCode());
        config.put("sdkCode", generateSdkCode());
        
        return Result.ok(config);
    }

    private String generateIframeCode() {
        return "<!-- 空白格AI客服 - iframe嵌入 -->\n" +
               "<iframe \n" +
               "  src=\"{SERVER_URL}/chat\" \n" +
               "  width=\"400\" \n" +
               "  height=\"600\" \n" +
               "  frameborder=\"0\"\n" +
               "  style=\"position: fixed; bottom: 20px; right: 20px; z-index: 9999; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.15);\"\n" +
               "></iframe>";
    }

    private String generateSdkCode() {
        return "<!-- 空白格AI客服 - JavaScript SDK -->\n" +
               "<div id=\"ai-cs-widget\"></div>\n" +
               "<script src=\"{SERVER_URL}/ai-cs-sdk.js\"></" + "script>\n" +
               "<script>\n" +
               "  AI_CS.init({\n" +
               "    container: '#ai-cs-widget',\n" +
               "    serverUrl: '{SERVER_URL}',\n" +
               "    position: 'bottom-right',\n" +
               "    theme: 'purple',\n" +
               "    title: 'AI智能客服'\n" +
               "  });\n" +
               "</" + "script>";
    }
}
