package org.max.authvs.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置
 * 配置所有接口返回都被 ResultDTO 对象包装
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("认证服务 API")
                        .description("提供用户认证和授权相关的 API 接口")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .url("https://example.com")))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("http://localhost:8080")
                        .description("本地开发环境"));
    }
}
