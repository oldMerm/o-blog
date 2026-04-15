package io.github.oldmerman.web.config;

import io.github.oldmerman.web.interceptor.AuthInterceptor;
import io.github.oldmerman.web.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.host}")
    private String origins;

    private final JwtInterceptor jwtInterceptor;

    private final AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = origins.split(",");
        registry.addMapping("/**")          // 所有接口
                .allowedOrigins(allowedOrigins) // 前端域名
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")        // GET/POST/PUT/DELETE...
                .allowedHeaders("*")
                .allowCredentials(true)     // 带 Cookie/Session 必须 true
                .maxAge(3600);              // 预检缓存 1 h
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).order(1)
                .addPathPatterns("/**");
        registry.addInterceptor(authInterceptor).order(2)
                .addPathPatterns(List.of("/admin/**","/version/admin/**"));
    }
}