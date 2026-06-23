package com.jnysx.aics.annotation;

import java.lang.annotation.*;

/**
 * 需要登录认证注解
 * 标记此注解的接口将检查用户 Session
 * @author 空白格·AI
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
    /**
     * 是否必须登录（默认 true）
     * 当类级别标记了 @RequireLogin 但方法不需要登录时，可用 @RequireLogin(false) 覆盖
     */
    boolean value() default true;
}
