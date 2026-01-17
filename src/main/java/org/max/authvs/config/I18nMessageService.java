package org.max.authvs.config;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化消息服务工具类
 * 简化在业务代码中获取国际化消息的使用
 */
@Component
public class I18nMessageService {

    private final MessageSource messageSource;

    public I18nMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 获取国际化消息
     *
     * @param key 消息键
     * @return 对应语言的消息值
     */
    public String getMessage(String key) {
        return getMessageWithArgs(key, null);
    }

    /**
     * 获取国际化消息（支持参数）
     *
     * @param key 消息键
     * @param args 消息参数
     * @return 对应语言的消息值
     */
    public String getMessageWithArgs(String key, Object[] args) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            // 如果消息不存在，返回key作为默认值
            return key;
        }
    }

    /**
     * 获取国际化消息（指定语言）
     *
     * @param key 消息键
     * @param locale 指定的语言区域
     * @return 对应语言的消息值
     */
    public String getMessageByLocale(String key, Locale locale) {
        return getMessageByLocaleWithArgs(key, null, locale);
    }

    /**
     * 获取国际化消息（指定语言和参数）
     *
     * @param key 消息键
     * @param args 消息参数
     * @param locale 指定的语言区域
     * @return 对应语言的消息值
     */
    public String getMessageByLocaleWithArgs(String key, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            // 如果消息不存在，返回key作为默认值
            return key;
        }
    }
}
