package com.jnysx.aics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI智能客服系统 - 启动类
 * 济南空白格网络科技有限公司
 * @version 1.0.0
 * @since 2026-05-19
 */
@SpringBootApplication
@MapperScan("com.jnysx.aics.mapper")
public class AiCsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiCsApplication.class, args);
    }
}
