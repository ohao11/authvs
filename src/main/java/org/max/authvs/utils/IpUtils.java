package org.max.authvs.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 */
public class IpUtils {

    private IpUtils() {
        // 工具类不允许实例化
    }

    /**
     * 获取客户端真实IP地址
     * 支持穿透代理和负载均衡
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getRealIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        // 尝试从X-Forwarded-For获取
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // 多级代理的情况，取第一个IP
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }

        // 尝试从X-Real-IP获取
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 尝试从Proxy-Client-IP获取
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 尝试从WL-Proxy-Client-IP获取
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 尝试从HTTP_CLIENT_IP获取
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 尝试从HTTP_X_FORWARDED_FOR获取
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }

        // 最后从RemoteAddr获取
        ip = request.getRemoteAddr();
        return StrUtil.isNotBlank(ip) ? ip : "";
    }

    /**
     * 检查IP是否有效
     *
     * @param ip IP地址
     * @return true=有效，false=无效
     */
    private static boolean isValidIp(String ip) {
        return StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip);
    }
}
