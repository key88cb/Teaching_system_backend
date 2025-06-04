package com.example.onlinetestingbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // 匹配所有以 /api 开头的接口
                .allowedOrigins("http://localhost:5173") // 允许来自 Vue 前端的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 支持的方法
                .allowedHeaders("*") // 所有头部都允许
                .exposedHeaders("Authorization") // 如果你用 token，可以暴露这个 header
                .allowCredentials(true); // 是否允许携带 cookie
    }
}