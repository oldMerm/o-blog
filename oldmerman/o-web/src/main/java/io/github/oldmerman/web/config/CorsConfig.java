package io.github.oldmerman.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")          // 所有接口
                .allowedOrigins("http://localhost:5173") // 前端域名
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")        // GET/POST/PUT/DELETE...
                .allowedHeaders("*")
                .allowCredentials(true)     // 带 Cookie/Session 必须 true
                .maxAge(3600);              // 预检缓存 1 h
    }
}