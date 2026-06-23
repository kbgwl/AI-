package com.jnysx.aics;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WechatControllerTest extends BaseIntegrationTest {

    @Test
    void verifyWechatServer() throws Exception {
        mockMvc.perform(get("/api/wechat/callback")
                .param("signature", "test")
                .param("timestamp", "1234567890")
                .param("nonce", "test123")
                .param("echostr", "hello"))
                .andExpect(status().isOk());
    }

    @Test
    void receiveTextMessage() throws Exception {
        String xml = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[你好]]></Content>" +
                "</xml>";

        mockMvc.perform(post("/api/wechat/callback")
                .contentType(MediaType.APPLICATION_XML)
                .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("xml")));
    }

    @Test
    void receiveOrderQueryMessage() throws Exception {
        String xml = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[订单查询]]></Content>" +
                "</xml>";

        mockMvc.perform(post("/api/wechat/callback")
                .contentType(MediaType.APPLICATION_XML)
                .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("订单")));
    }

    @Test
    void receiveRefundMessage() throws Exception {
        String xml = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[我要退货]]></Content>" +
                "</xml>";

        mockMvc.perform(post("/api/wechat/callback")
                .contentType(MediaType.APPLICATION_XML)
                .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("退货")));
    }

    @Test
    void receiveProductMessage() throws Exception {
        String xml = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[产品功能]]></Content>" +
                "</xml>";

        mockMvc.perform(post("/api/wechat/callback")
                .contentType(MediaType.APPLICATION_XML)
                .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("产品")));
    }

    @Test
    void receiveTransferMessage() throws Exception {
        String xml = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[转人工]]></Content>" +
                "</xml>";

        mockMvc.perform(post("/api/wechat/callback")
                .contentType(MediaType.APPLICATION_XML)
                .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("人工")));
    }

    @Test
    void receiveSubscribeEvent() throws Exception {
        String xml = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[event]]></MsgType>" +
                "<Event><![CDATA[subscribe]]></Event>" +
                "</xml>";

        mockMvc.perform(post("/api/wechat/callback")
                .contentType(MediaType.APPLICATION_XML)
                .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("欢迎")));
    }

    @Test
    void receiveUnsubscribeEvent() throws Exception {
        String xml = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[event]]></MsgType>" +
                "<Event><![CDATA[unsubscribe]]></Event>" +
                "</xml>";

        mockMvc.perform(post("/api/wechat/callback")
                .contentType(MediaType.APPLICATION_XML)
                .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    @Test
    void getWechatConfig() throws Exception {
        mockMvc.perform(get("/api/wechat/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void wechatCallbackDoesNotRequireLogin() throws Exception {
        mockMvc.perform(get("/api/wechat/callback")
                .param("signature", "test")
                .param("timestamp", "1234567890")
                .param("nonce", "test123")
                .param("echostr", "hello"))
                .andExpect(status().isOk());
    }
}