package com.example.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 允许前端本地开发地址访问
 * 
 * @author Library Management System
 */
@Configuration
public class CorsConfig {

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();

    // 允许的前端地址
    config.addAllowedOrigin("http://localhost:5173");
    config.addAllowedOrigin("http://127.0.0.1:5173");

    // 允许的请求方法
    config.addAllowedMethod("GET");
    config.addAllowedMethod("POST");
    config.addAllowedMethod("PUT");
    config.addAllowedMethod("DELETE");
    config.addAllowedMethod("OPTIONS");

    // 允许的请求头
    config.addAllowedHeader("*");

    // 允许携带凭证
    config.setAllowCredentials(true);

    // 预检请求的有效期（秒）
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsFilter(source);
  }
}
