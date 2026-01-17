package org.max.authvs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * 国际化（i18n）配置类
 * 支持多语言消息处理，自动检测请求中的语言偏好
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * 配置LocaleResolver：根据HTTP请求的Accept-Language头自动检测语言
     * 默认为中文（简体）
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        localeResolver.setSupportedLocales(List.of(
                Locale.SIMPLIFIED_CHINESE,
                Locale.CHINA,
                Locale.ENGLISH,
                Locale.US,
                Locale.UK
        ));
        return localeResolver;
    }

    /**
     * 配置MessageSource：加载国际化资源文件
     * 资源文件位置：src/main/resources/i18n/messages_*.properties
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("i18n/messages");
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        source.setCacheSeconds(3600); // 缓存1小时
        return source;
    }
}
