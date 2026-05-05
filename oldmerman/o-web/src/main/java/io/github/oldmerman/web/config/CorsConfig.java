package io.github.oldmerman.web.config;

import io.github.oldmerman.web.interceptor.AuthInterceptor;
import io.github.oldmerman.web.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.host}")
    private String origins;

    private final JwtInterceptor jwtInterceptor;

    private final AuthInterceptor authInterceptor;

    /*    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = origins.split(",");
        registry.addMapping("/**")          // 所有接口
                .allowedOrigins(allowedOrigins) // 前端域名
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")        // GET/POST/PUT/DELETE...
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)     // 带 Cookie/Session 必须 true
                .maxAge(3600);              // 预检缓存 1 h
    }*/

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        String[] allowedOrigins = origins.split(",");
        Arrays.stream(allowedOrigins).forEach(config::addAllowedOriginPattern); // 前端域名

        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.addExposedHeader("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new
                UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // 最高优先级
        return bean;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).order(1)
                .addPathPatterns("/**");
        registry.addInterceptor(authInterceptor).order(2)
                .addPathPatterns(List.of("/admin/**","/version/admin/**"))
                .excludePathPatterns(List.of("/admin/usr/isAdmin"));
    }
}