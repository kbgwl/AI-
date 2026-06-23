package com.jnysx.aics.tenant;

import com.jnysx.aics.entity.Agent;
import com.jnysx.aics.service.SystemConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Long tenantId = null;

        HttpSession session = request.getSession(false);
        if (session != null) {
            Agent agent = (Agent) session.getAttribute("user");
            if (agent != null && agent.getTenantId() != null) {
                tenantId = agent.getTenantId();
            }
        }

        if (tenantId == null) {
            String header = request.getHeader("X-Tenant-Id");
            if (header != null && !header.isEmpty()) {
                try {
                    tenantId = Long.parseLong(header);
                } catch (NumberFormatException ignored) {}
            }
        }

        TenantContext.setTenantId(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
