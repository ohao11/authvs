package org.max.authvs.utils;

/**
 * 敏感数据脱敏工具类
 */
public class SensitiveDataUtils {

    private SensitiveDataUtils() {
        // 工具类不允许实例化
    }

    /**
     * 邮箱脱敏
     * 保留前2个字符和@后面的域名，中间用***代替
     * 例如：example@gmail.com -> ex***@gmail.com
     *
     * @param email 原始邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email; // 无效邮箱格式，返回原值
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        // 如果本地部分长度小于等于2，全部保留
        if (localPart.length() <= 2) {
            return localPart + "***" + domain;
        }

        // 保留前2个字符
        return localPart.substring(0, 2) + "***" + domain;
    }

    /**
     * 手机号脱敏
     * 保留前3位和后4位，中间用****代替
     * 例如：13812345678 -> 138****5678
     *
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }

        // 移除所有非数字字符
        String digits = phone.replaceAll("\\D", "");

        // 如果长度小于7，无法进行有效脱敏，返回原值
        if (digits.length() < 7) {
            return phone;
        }

        // 如果长度为11位（标准手机号），保留前3位和后4位
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "****" + digits.substring(7);
        }

        // 其他长度，保留前3位和后4位，中间用****代替
        if (digits.length() > 7) {
            return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 4);
        }

        return phone;
    }
}
