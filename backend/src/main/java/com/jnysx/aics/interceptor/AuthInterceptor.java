package com.jnysx.aics.interceptor;

import com.jnysx.aics.annotation.RequireLogin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * 认证拦截器
 * 检查需要登录的接口是否有有效 Session
 * @author 空白格·AI
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

 @Override
 public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
 // 跨域请求时，如果是 OPTIONS 请求，直接放行
 if (request.getMethod().equals("OPTIONS")) {
 return true;
 }

 // 只处理方法类型的请求
 if (!(handler instanceof HandlerMethod)) {
 return true;
 }

 HandlerMethod handlerMethod = (HandlerMethod) handler;
 Method method = handlerMethod.getMethod();
 Class<?> beanType = handlerMethod.getBeanType();

// 检查方法或类上是否有 @RequireLogin 注解
RequireLogin requireLogin = method.getAnnotation(RequireLogin.class);
if (requireLogin == null) {
  requireLogin = beanType.getAnnotation(RequireLogin.class);
}

 boolean requireLoginFlag = requireLogin != null && requireLogin.value();

if (!requireLoginFlag) {
  // 不需要登录的接口，直接放行
  return true;
}

 // 需要登录，检查 Session
 HttpSession session = request.getSession(false);
 if (session == null) {
 response.setStatus(401);
 response.setContentType("application/json;charset=UTF-8");
 response.getWriter().write("{\"code\":401,\"message\":\"未登录或 Session 已过期\"}");
 return false;
 }

 // 检查 Session 中是否有用户信息
 Object userId = session.getAttribute("userId");
 if (userId == null) {
 response.setStatus(401);
 response.setContentType("application/json;charset=UTF-8");
 response.getWriter().write("{\"code\":401,\"message\":\"未登录或 Session 已过期\"}");
 return false;
 }

 // 验证通过，放行
 return true;
 }
}
