package com.jnysx.aics.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 输入安全过滤服务
 * 提供 XSS 防护和 SQL 注入检测
 */
@Service
public class InputSanitizer {

    private static final Logger log = LoggerFactory.getLogger(InputSanitizer.class);

    /**
     * XSS 防护：移除危险标签和脚本
     */
    public String sanitize(String input) {
        if (input == null) return "";
        return input
            .replaceAll("<script[^>]*>.*?</script>", "")
            .replaceAll("<iframe[^>]*>.*?</iframe>", "")
            .replaceAll("<[^>]+>", "")
            .replaceAll("javascript:", "")
            .replaceAll("on\\w+=", "")
            .trim();
    }

    /**
     * SQL 注入检测：检查常见注入模式
     */
    public boolean containsSqlInjection(String input) {
        if (input == null) return false;

        String lower = input.toLowerCase();

        String[] sqlPatterns = {
            "' or '1'='1",
            "'; drop table",
            "'; delete from",
            "union select",
            "union all select",
            "' or 1=1--",
            "admin'--",
            "or 'a'='a",
            "'; exec master",
            "xp_cmdshell",
            "sp_executesql",
            "waitfor delay",
            "benchmark(",
            "sleep(",
            "information_schema",
            "sys.tables",
            "--",
            "/* */",
        };

        for (String pattern : sqlPatterns) {
            if (lower.contains(pattern)) {
                return true;
            }
        }

        int quoteCount = 0;
        for (char c : input.toCharArray()) {
            if (c == '\'') quoteCount++;
        }
        if (quoteCount > 5) {
            return true;
        }

        return false;
    }
}
