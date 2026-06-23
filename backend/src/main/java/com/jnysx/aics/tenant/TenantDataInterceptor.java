package com.jnysx.aics.tenant;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, org.apache.ibatis.session.RowBounds.class, org.apache.ibatis.session.ResultHandler.class})
})
public class TenantDataInterceptor implements Interceptor {

    @Override
    @SuppressWarnings("unchecked")
    public Object intercept(Invocation invocation) throws Exception {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            Object param = invocation.getArgs()[1];
            if (param instanceof java.util.Map) {
                ((java.util.Map<String, Object>) param).put("tenantId", tenantId);
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {}
}
