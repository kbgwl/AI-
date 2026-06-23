package com.jnysx.aics.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentGatewayService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /**
     * 支付宝退款
     */
    public JSONObject alipayRefund(String appId, String privateKey, String alipayPublicKey,
                                   String tradeNo, String outTradeNo, String refundAmount, String reason) {
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("app_id", appId);
            params.put("method", "alipay.trade.refund");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            params.put("version", "1.0");
            params.put("notify_url", "");

            JSONObject bizContent = new JSONObject();
            bizContent.put("trade_no", tradeNo);
            bizContent.put("out_trade_no", outTradeNo);
            bizContent.put("refund_amount", refundAmount);
            bizContent.put("refund_reason", reason != null ? reason : "用户申请退款");
            params.put("biz_content", bizContent.toJSONString());

            String signContent = params.entrySet().stream()
                    .filter(e -> e.getValue() != null && !e.getValue().isEmpty() && !"sign".equals(e.getKey()))
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
            String sign = rsa2Sign(signContent, privateKey);
            params.put("sign", sign);

            String formBody = params.entrySet().stream()
                    .map(e -> e.getKey() + "=" + java.net.URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openapi.alipay.com/gateway/trade/refund"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject resp = JSON.parseObject(response.body());
            JSONObject alipayResponse = resp.getJSONObject("alipay_trade_refund_response");

            JSONObject result = new JSONObject();
            if ("10000".equals(alipayResponse.getString("code"))) {
                result.put("success", true);
                result.put("tradeNo", alipayResponse.getString("trade_no"));
                result.put("refundNo", alipayResponse.getString("trade_no"));
                result.put("message", "退款成功");
            } else {
                result.put("success", false);
                result.put("message", alipayResponse.getString("sub_msg"));
                result.put("code", alipayResponse.getString("code"));
            }
            return result;
        } catch (Exception e) {
            log.error("Alipay refund failed: {}", e.getMessage(), e);
            return errorResult("支付宝退款异常：" + e.getMessage());
        }
    }

    /**
     * 微信支付退款
     */
    public JSONObject wechatRefund(String mchId, String apiKey, String serialNo,
                                   String transactionId, String outTradeNo, int refundAmount, String reason) {
        try {
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            JSONObject requestBody = new JSONObject();
            requestBody.put("transaction_id", transactionId);
            requestBody.put("out_trade_no", outTradeNo);
            requestBody.put("notify_url", "");

            JSONObject amountObj = new JSONObject();
            amountObj.put("refund", refundAmount);
            amountObj.put("total", refundAmount);
            amountObj.put("currency", "CNY");
            requestBody.put("amount", amountObj);
            requestBody.put("reason", reason != null ? reason : "用户申请退款");

            String body = requestBody.toJSONString();

            String method = "POST";
            String url = "/v3/refund/domestic/refunds";
            String signStr = method + "\n" + url + "\n" + timestamp + "\n" + nonceStr + "\n" + body + "\n";
            String authorization = buildWechatAuth(mchId, nonceStr, timestamp, serialNo, signStr, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mch.weixin.qq.com/v3/refund/domestic/refunds"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authorization)
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject resp = JSON.parseObject(response.body());

            JSONObject result = new JSONObject();
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                String status = resp.getString("status");
                if ("SUCCESS".equals(status) || "PROCESSING".equals(status)) {
                    result.put("success", true);
                    result.put("refundId", resp.getString("refund_id"));
                    result.put("message", "退款" + ("SUCCESS".equals(status) ? "成功" : "处理中"));
                } else {
                    result.put("success", false);
                    result.put("message", resp.getString("status"));
                }
            } else {
                result.put("success", false);
                JSONObject errCode = resp.getJSONObject("error");
                result.put("message", errCode != null ? errCode.getString("message") : "退款失败");
            }
            return result;
        } catch (Exception e) {
            log.error("WeChat refund failed: {}", e.getMessage(), e);
            return errorResult("微信退款异常：" + e.getMessage());
        }
    }

    private String rsa2Sign(String content, String privateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", ""));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey key = keyFactory.generatePrivate(keySpec);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private String buildWechatAuth(String mchId, String nonceStr, String timestamp,
                                    String serialNo, String message, String apiKey) throws Exception {
        String sign = sha256Hex(message + apiKey);
        return "WECHATPAY2-SHA256-RSA2048 mchid=\"" + mchId + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + serialNo + "\","
                + "signature=\"" + sign + "\"";
    }

    private String sha256Hex(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private JSONObject errorResult(String message) {
        JSONObject result = new JSONObject();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
