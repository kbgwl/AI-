package com.jnysx.aics.service;

import com.jnysx.aics.entity.Intent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class IntentRecognitionServiceTest {

    @Autowired
    private IntentRecognitionService intentRecognitionService;

    @Test
    void recognizeEmptyInputReturnsUnknown() {
        IntentRecognitionService.IntentResult result = intentRecognitionService.recognize("");
        assertEquals("unknown", result.getIntentCode());
        assertEquals(0.0, result.getConfidence());
        assertEquals("empty", result.getMatchType());
    }

    @Test
    void recognizeNullInputReturnsUnknown() {
        IntentRecognitionService.IntentResult result = intentRecognitionService.recognize(null);
        assertEquals("unknown", result.getIntentCode());
    }

    @Test
    void recognizeWhitespaceOnlyReturnsUnknown() {
        IntentRecognitionService.IntentResult result = intentRecognitionService.recognize("   ");
        assertEquals("unknown", result.getIntentCode());
    }

    @Test
    void recognizeOrderQueryByRegex() {
        IntentRecognitionService.IntentResult result = intentRecognitionService.recognize("查一下订单号12345");
        assertEquals("query_order", result.getIntentCode());
        assertEquals("regex", result.getMatchType());
    }

    @Test
    void recognizeRefundByKeyword() {
        IntentRecognitionService.IntentResult result = intentRecognitionService.recognize("我要退货");
        assertEquals("refund", result.getIntentCode());
    }

    @Test
    void recognizeTransferManualByRegex() {
        IntentRecognitionService.IntentResult result = intentRecognitionService.recognize("转人工客服");
        assertEquals("transfer_manual", result.getIntentCode());
        assertTrue(result.isHighConfidence());
    }

    @Test
    void recognizePriceInquiryByRegex() {
        IntentRecognitionService.IntentResult result = intentRecognitionService.recognize("这个多少钱");
        assertEquals("price_inquiry", result.getIntentCode());
    }

    @Test
    void recognizeProductInfoByRegex() {
        IntentRecognitionService.IntentResult result = intentRecognitionService.recognize("你们的产品有什么功能");
        assertEquals("product_info", result.getIntentCode());
        assertEquals("regex", result.getMatchType());
    }

    @Test
    void recognizeUnknownInputReturnsFallback() {
        IntentRecognitionService.IntentResult result = intentRecognitionService.recognize("今天天气真好");
        assertEquals("unknown", result.getIntentCode());
        assertEquals("fallback", result.getMatchType());
    }

    @Test
    void intentResultHighConfidenceThreshold() {
        IntentRecognitionService.IntentResult high = new IntentRecognitionService.IntentResult("test", 0.7, "test");
        IntentRecognitionService.IntentResult low = new IntentRecognitionService.IntentResult("test", 0.69, "test");
        assertTrue(high.isHighConfidence());
        assertFalse(low.isHighConfidence());
    }
}