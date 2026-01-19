package org.max.authvs.utils;

/**
 * 敏感数据脱敏工具类
 * 对于hutool没有的脱敏方法，在此类中定义
 */
public class SensitiveDataUtils {

    private SensitiveDataUtils() {
        // 工具类不允许实例化
    }

    /**
     * JSON字符串中的密码字段脱敏
     * Hutool没有直接的JSON密码脱敏方法，在此自定义实现
     * 将password、newPassword、oldPassword等敏感字段的值替换为******
     *
     * @param json 原始JSON字符串
     * @return 脱敏后的JSON字符串
     */
    public static String maskPasswordInJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        // 脱敏密码字段
        String result = json.replaceAll("(\"password\"\\s*:\\s*\")([^\"]+)(\")", "$1******$3");
        result = result.replaceAll("(\"newPassword\"\\s*:\\s*\")([^\"]+)(\")", "$1******$3");
        result = result.replaceAll("(\"oldPassword\"\\s*:\\s*\")([^\"]+)(\")", "$1******$3");
        return result;
    }
}