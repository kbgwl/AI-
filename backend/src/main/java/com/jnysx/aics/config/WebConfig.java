package com.jnysx.aics.config;

import com.jnysx.aics.interceptor.AuthInterceptor;
import com.jnysx.aics.interceptor.LicenseInterceptor;
import com.jnysx.aics.interceptor.PerformanceInterceptor;
import com.jnysx.aics.interceptor.RateLimitInterceptor;
import com.jnysx.aics.tenant.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;
    @Autowired
    private PerformanceInterceptor performanceInterceptor;
    @Autowired
    private LicenseInterceptor licenseInterceptor;
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;
    @Autowired
    private TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .order(0);

        registry.addInterceptor(licenseInterceptor)
                .addPathPatterns("/api/**")
                .order(1);

        registry.addInterceptor(performanceInterceptor)
                .addPathPatterns("/api/**")
                .order(2);

        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**")
                .order(3);

        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .order(4)
                .excludePathPatterns(
                    "/api/chat/**",
                    "/api/kb/items",
                    "/api/kb/item/**",
                    "/api/kb/categories",
                    "/api/kb/industries",
                    "/api/ticket/create",
                    "/api/wechat/**",
                    "/api/license/**",
                    "/login",
                    "/api/admin/login",
                    "/**/favicon.ico"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
