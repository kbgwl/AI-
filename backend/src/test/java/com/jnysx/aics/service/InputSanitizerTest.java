package com.jnysx.aics.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputSanitizerTest {

    private InputSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        sanitizer = new InputSanitizer();
    }

    @Test
    void sanitizeNullInputReturnsEmpty() {
        assertEquals("", sanitizer.sanitize(null));
    }

    @Test
    void sanitizeRemovesScriptTags() {
        String input = "hello <script>alert('xss')</script> world";
        assertEquals("hello  world", sanitizer.sanitize(input));
    }

    @Test
    void sanitizeRemovesIframeTags() {
        String input = "text <iframe src='evil.com'></iframe> end";
        assertEquals("text  end", sanitizer.sanitize(input));
    }

    @Test
    void sanitizeRemovesHtmlTags() {
        String input = "hello <b>bold</b> <i>italic</i>";
        assertEquals("hello bold italic", sanitizer.sanitize(input));
    }

    @Test
    void sanitizeRemovesJavascriptProtocol() {
        String input = "click javascript:alert(1)";
        assertEquals("click alert(1)", sanitizer.sanitize(input));
    }

    @Test
    void sanitizeRemovesEventHandlerAttributes() {
        String input = "div onclick=alert(1) onmouseover=hack()";
        assertEquals("div alert(1) hack()", sanitizer.sanitize(input));
    }

    @Test
    void sanitizeNormalTextUnchanged() {
        String input = "你好，我想退货";
        assertEquals("你好，我想退货", sanitizer.sanitize(input));
    }

    @Test
    void containsSqlInjectionDetectsClassicPayload() {
        assertTrue(sanitizer.containsSqlInjection("' or '1'='1"));
    }

    @Test
    void containsSqlInjectionDetectsDropTable() {
        assertTrue(sanitizer.containsSqlInjection("'; drop table users"));
    }

    @Test
    void containsSqlInjectionDetectsUnionSelect() {
        assertTrue(sanitizer.containsSqlInjection("1 union select * from passwords"));
    }

    @Test
    void containsSqlInjectionDetectsCommentPattern() {
        assertTrue(sanitizer.containsSqlInjection("admin'--"));
    }

    @Test
    void containsSqlInjectionDetectsHighQuoteCount() {
        String input = "a'b'c'd'e'f'g";
        assertTrue(sanitizer.containsSqlInjection(input));
    }

    @Test
    void containsSqlInjectionReturnsFalseForNormalText() {
        assertFalse(sanitizer.containsSqlInjection("我想查询订单"));
    }

    @Test
    void containsSqlInjectionReturnsFalseForNull() {
        assertFalse(sanitizer.containsSqlInjection(null));
    }

    @Test
    void containsSqlInjectionDetectsSleep() {
        assertTrue(sanitizer.containsSqlInjection("1 and sleep(5)"));
    }

    @Test
    void containsSqlInjectionDetectsInformationSchema() {
        assertTrue(sanitizer.containsSqlInjection("select from information_schema.tables"));
    }
}