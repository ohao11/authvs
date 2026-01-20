package org.max.authvs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 跨域资源共享（CORS）配置
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源（前端地址）
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Vite 开发服务器
                "http://127.0.0.1:5173",
                "http://localhost:3000",  // 备用前端端口
                "http://127.0.0.1:3000"
        ));

        // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // 允许携带凭证（cookies）
        configuration.setAllowCredentials(true);

        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"
        ));

        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
