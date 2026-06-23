# ProGuard配置 - AI智能客服系统后端混淆
# 使用方式: 先mvn package, 然后 proguard @proguard.pro
# 或使用 build-prod.sh 一键构建

# 输入JAR (Spring Boot fat JAR)
-injars 'target/ai-customer-service-1.0.0.jar'
-outjars 'target/ai-customer-service-1.0.0-obf.jar'

# Java库
-libraryjars <java.home>/lib/jrt-fs.jar

# 保留Spring Boot启动类
-keep class com.jnysx.aics.AiCsApplication {
    public static void main(java.lang.String[]);
}

# 保留Spring注解的类
-keep @org.springframework.stereotype.Service class * { *; }
-keep @org.springframework.stereotype.Component class * { *; }
-keep @org.springframework.web.bind.annotation.RestController class * { *; }
-keep @org.springframework.context.annotation.Configuration class * { *; }

# 保留实体类（MyBatis-Plus需要）
-keep class com.jnysx.aics.entity.** { *; }

# 保留Mapper接口
-keep class com.jnysx.aics.mapper.** { *; }

# 保留Service接口
-keep interface com.jnysx.aics.service.** { *; }

# 保留Controller方法（API端点）
-keepclassmembers class * {
    @org.springframework.web.bind.annotation.RequestMapping <methods>;
    @org.springframework.web.bind.annotation.GetMapping <methods>;
    @org.springframework.web.bind.annotation.PostMapping <methods>;
    @org.springframework.web.bind.annotation.PutMapping <methods>;
    @org.springframework.web.bind.annotation.DeleteMapping <methods>;
}

# 保留枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留Lombok生成的方法
-keep class lombok.** { *; }
-keep class * extends lombok.AnnotationAccessor { *; }

# 保留FastJSON
-keep class com.alibaba.fastjson2.** { *; }
-dontwarn com.alibaba.fastjson2.**

# 保留MyBatis-Plus
-keep class com.baomidou.** { *; }
-dontwarn com.baomidou.**

# 保留OkHttp
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# 保留JWT
-keep class io.jsonwebtoken.** { *; }
-dontwarn io.jsonwebtoken.**

# 保留WebSocket
-keep class javax.websocket.** { *; }
-keep class org.springframework.web.socket.** { *; }

# 保留Spring内部类
-keep class org.springframework.** { *; }
-dontwarn org.springframework.**

# 混淆选项
-repackageclasses 'com.aics.obfuscated'
-allowaccessmodification
-optimizationpasses 3
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclassmembers

# 处理Spring Boot BOOT-INF结构
-dontwarn proguard.**
