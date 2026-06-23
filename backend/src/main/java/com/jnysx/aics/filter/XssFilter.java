package com.jnysx.aics.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * XSS过滤器 - 防止跨站脚本攻击
 * 自动转义请求参数中的HTML标签
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contentType = request.getContentType();
        if (contentType != null && (contentType.contains("multipart/form-data"))) {
            chain.doFilter(req, res);
            return;
        }

        chain.doFilter(new XssRequestWrapper(request), response);
    }

    public static class XssRequestWrapper extends HttpServletRequestWrapper {

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return value != null ? stripXss(value) : null;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] cleanValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleanValues[i] = stripXss(values[i]);
            }
            return cleanValues;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return value != null ? stripXss(value) : null;
        }

        private String stripXss(String value) {
            if (value == null) return null;
            value = value.replaceAll("<script[^>]*?>[\\s\\S]*?<\\/script>", "");
            value = value.replaceAll("<script[^>]*?>", "");
            value = value.replaceAll("</script>", "");
            value = value.replaceAll("javascript:", "");
            value = value.replaceAll("on\\w+\\s*=", "");
            value = value.replaceAll("eval\\s*\\(", "");
            value = value.replaceAll("expression\\s*\\(", "");
            value = value.replaceAll("<iframe[^>]*?>", "");
            value = value.replaceAll("</iframe>", "");
            value = value.replaceAll("<object[^>]*?>", "");
            value = value.replaceAll("</object>", "");
            value = value.replaceAll("<embed[^>]*?>", "");
            return value;
        }
    }
}
