package com.jnysx.aics.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jnysx.aics.entity.ChannelConfig;
import com.jnysx.aics.entity.Message;
import com.jnysx.aics.entity.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信渠道服务
 * 处理微信公众号/小程序消息接收与回复
 * @author 济南空白格网络科技有限公司
 */
@Service
public class WechatService {
    
    private static final Logger log = LoggerFactory.getLogger(WechatService.class);

    private ChannelConfigService channelConfigService;
    private SessionService sessionService;
    private MessageService messageService;
    
    public WechatService(ChannelConfigService channelConfigService, 
                         SessionService sessionService, 
                         MessageService messageService) {
        this.channelConfigService = channelConfigService;
        this.sessionService = sessionService;
        this.messageService = messageService;
    }

    /**
     * 验证微信服务器
     */
    public String verify(String signature, String timestamp, String nonce, String echostr) {
        try {
            // 获取微信配置
            LambdaQueryWrapper<ChannelConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChannelConfig::getChannel, "wechat_mp");
            ChannelConfig config = channelConfigService.getOne(wrapper);
            
            if (config == null || config.getConfigData() == null) {
                return "";
            }

            JSONObject configData = JSONObject.parseObject(config.getConfigData());
            String token = configData.getString("token");

            if (token == null) {
                return "";
            }

            // 排序并 SHA1 加密
            String[] arr = new String[] { token, timestamp, nonce };
            Arrays.sort(arr);
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(s);
            }

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String computedSignature = hexString.toString();

            if (computedSignature.equals(signature)) {
                return echostr;
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 处理微信消息
     */
    public String processMessage(String requestBody) {
        try {

            // 解析 XML
            Map<String, String> msg = parseXml(requestBody);
            log.info("解析结果：FromUserName={}, MsgType={}, Content={}", 
                msg.get("FromUserName"), msg.get("MsgType"), msg.get("Content"));

            String fromUser = msg.get("FromUserName");
            String toUser = msg.get("ToUserName");
            String msgType = msg.get("MsgType");
            String content = msg.get("Content");
            String event = msg.get("Event");

            if (fromUser == null || msgType == null) {
                return "";
            }

            // 事件消息
            if ("event".equalsIgnoreCase(msgType)) {
                if ("subscribe".equalsIgnoreCase(event)) {
                    ChannelConfig config = getWechatConfig();
                    String welcomeMsg = config != null && config.getWelcomeMessage() != null 
                        ? config.getWelcomeMessage() 
                        : "您好！欢迎使用空白格 AI 客服！";
                    return buildTextResponse(fromUser, toUser, welcomeMsg);
                } else if ("unsubscribe".equalsIgnoreCase(event)) {
                    return "";
                }
                return "";
            }

            // 文本消息
            if ("text".equalsIgnoreCase(msgType)) {
                // 创建或获取会话
                String sessionId = "wx_" + fromUser;
                Session session = getSessionBySessionId(sessionId);
                
                if (session == null) {
                    session = new Session();
                    session.setSessionId(sessionId);
                    session.setChannel("wechat_mp");
                    session.setStatus(1); // 进行中
                    sessionService.save(session);
                }

                // 保存用户消息
                Message userMsg = new Message();
                userMsg.setSessionId(session.getId().toString());
                userMsg.setContent(content);
                userMsg.setSenderType("0"); // 用户
                userMsg.setCreateTime(java.time.LocalDateTime.now());
                messageService.save(userMsg);

                // 生成回复
                String reply = generateSimpleReply(content);

                // 保存 AI 回复
                Message aiMsg = new Message();
                aiMsg.setSessionId(session.getId().toString());
                aiMsg.setContent(reply);
                aiMsg.setSenderType("1"); // AI
                aiMsg.setCreateTime(java.time.LocalDateTime.now());
                messageService.save(aiMsg);

                return buildTextResponse(fromUser, toUser, reply);
            }
            return "";

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取微信配置
     */
    private ChannelConfig getWechatConfig() {
        LambdaQueryWrapper<ChannelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelConfig::getChannel, "wechat_mp");
        return channelConfigService.getOne(wrapper);
    }

    /**
     * 根据 sessionId 获取会话
     */
    private Session getSessionBySessionId(String sessionId) {
        LambdaQueryWrapper<Session> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Session::getSessionId, sessionId);
        return sessionService.getOne(wrapper);
    }

    /**
     * 简单回复规则（MVP 版本）
     */
    private String generateSimpleReply(String content) {
        if (content.contains("你好") || content.contains("您好")) {
            return "您好！欢迎使用空白格 AI 客服！请问有什么可以帮您？\n\n回复关键词获取帮助：\n1️⃣ 查订单\n2️⃣ 退货\n3️⃣ 产品咨询\n4️⃣ 转人工";
        } else if (content.contains("订单") || content.contains("物流")) {
            return "📦 **订单查询**\n\n请提供您的订单号，或者告诉我您购买的商品名称和下单时间。\n\n支持查询方式：\n1. 订单号查询（最快）\n2. 手机号查询\n3. 商品名 + 时间查询";
        } else if (content.contains("退货") || content.contains("退款")) {
            return "🔄 **退货流程**\n\n1️⃣ 登录账户 → 我的订单\n2️⃣ 选择需要退货的订单 → 申请退货\n3️⃣ 填写退货原因并提交\n4️⃣ 等待审核（1-2 个工作日）\n5️⃣ 审核通过后寄回商品\n\n⚠️ 注意：商品需保持原包装未使用，7 天内可申请";
        } else if (content.contains("产品") || content.contains("功能")) {
            return "🤖 我们提供以下 AI 产品和服务：\n\n✅ AI 客服系统 — 7×24 小时智能接待\n✅ AI 出图 — 电商商品图一键生成\n✅ AI 短视频 — 智能剪辑配乐\n\n请问您对哪款产品感兴趣？";
        } else if (content.contains("人工") || content.contains("客服")) {
            return "👩‍💼 正在为您转接人工客服，请稍候...\n\n当前在线客服：3 人\n预计等待时间：1-2 分钟\n\n（此为 MVP 演示，实际转接功能待开发）";
        } else {
            return "抱歉，我还没学会这个问题 😅\n\n您可以回复以下关键词获取帮助：\n- 你好\n- 订单\n- 退货\n- 产品\n- 人工";
        }
    }

    /**
     * 构建文本回复 XML
     */
    private String buildTextResponse(String fromUser, String toUser, String content) {
        long createTime = System.currentTimeMillis() / 1000;
        return String.format(
            "<xml>" +
            "<ToUserName><![CDATA[%s]]></ToUserName>" +
            "<FromUserName><![CDATA[%s]]></FromUserName>" +
            "<CreateTime>%d</CreateTime>" +
            "<MsgType><![CDATA[text]]></MsgType>" +
            "<Content><![CDATA[%s]]></Content>" +
            "</xml>",
            fromUser, toUser, createTime, content
        );
    }

    /**
     * 简单解析 XML
     */
    private Map<String, String> parseXml(String xml) {
        Map<String, String> map = new HashMap<>();
        String[] tags = {"FromUserName", "ToUserName", "CreateTime", "MsgType", "Content", "Event", "EventKey"};
        
        for (String tag : tags) {
            // 尝试 CDATA 格式
            String regex = "<" + tag +"><!\\[CDATA\\[([^]]+)]][^<]*</" + tag + ">";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(xml);
            if (matcher.find()) {
                String value = matcher.group(1).trim();
                // 清理 CDATA 残留
                value = value.replace("<![CDATA[", "").replace("]]>", "").trim();
                map.put(tag, value);
            }
        }
        
        return map;
    }
}