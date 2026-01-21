package org.max.authvs.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.max.authvs.annotation.OperationLog;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.service.OperationLogService;
import org.max.authvs.utils.IpUtils;
import org.max.authvs.utils.SecurityUtils;
import org.max.authvs.utils.SensitiveDataUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    // 请求参数最大长度
    private static final int MAX_PARAM_LENGTH = 2000;

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("@annotation(org.max.authvs.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        org.max.authvs.entity.OperationLog operationLog = new org.max.authvs.entity.OperationLog();

        // 获取请求信息
        HttpServletRequest request = getRequest();
        if (request != null) {
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setIpAddress(IpUtils.getRealIp(request));
            operationLog.setUserAgent(request.getHeader("User-Agent"));
            // 从请求属性中获取登录时设置的用户信息（登录阶段SecurityContext尚未建立）
            Object uidAttr = request.getAttribute("OP_USER_ID");
            Object unameAttr = request.getAttribute("OP_USERNAME");
            if (uidAttr instanceof Long uid) {
                operationLog.setUserId(uid);
            }
            if (unameAttr instanceof String uname) {
                operationLog.setUsername(uname);
            }
        }

        // 获取用户信息
        try {
            if (SecurityUtils.isAuthenticated()) {
                CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
                operationLog.setUserId(currentUser.getId());
                operationLog.setUsername(currentUser.getUsername());
            }
        } catch (Exception e) {
            // 匿名访问，不记录用户信息
        }

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        operationLog.setOperationType(annotation.type().name());
        operationLog.setOperationModule(annotation.module());
        operationLog.setOperationDesc(annotation.description());

        // 根据请求URL判断平台类型
        if (operationLog.getRequestUrl() != null) {
            if (operationLog.getRequestUrl().startsWith("/api/admin") || 
                operationLog.getRequestUrl().startsWith("/api/admins") ||
                operationLog.getRequestUrl().startsWith("/api/operation-logs")) {
                operationLog.setPlatformType(2); // 后台管理
            } else {
                operationLog.setPlatformType(1); // 门户
            }
        } else {
            operationLog.setPlatformType(1); // 默认为门户
        }

        // 记录请求参数
        if (annotation.recordParams()) {
            String params = getRequestParams(joinPoint);
            operationLog.setRequestParams(StrUtil.maxLength(params, MAX_PARAM_LENGTH));
        }

        Object result;
        try {
            // 执行目标方法
            result = joinPoint.proceed();

            operationLog.setStatus("SUCCESS");

            // 记录返回结果
            if (annotation.recordResult() && result != null) {
                String resultStr = JSONUtil.toJsonStr(result);
                operationLog.setResponseResult(StrUtil.maxLength(resultStr, MAX_PARAM_LENGTH));
            }
            // 若登录过程中未能获取用户信息，尝试在方法执行后从请求属性或安全上下文填充
            if ((operationLog.getUserId() == null || operationLog.getUsername() == null) && request != null) {
                Object uidAttr2 = request.getAttribute("OP_USER_ID");
                Object unameAttr2 = request.getAttribute("OP_USERNAME");
                if (operationLog.getUserId() == null && uidAttr2 instanceof Long uid2) {
                    operationLog.setUserId(uid2);
                }
                if (operationLog.getUsername() == null && unameAttr2 instanceof String uname2) {
                    operationLog.setUsername(uname2);
                }
            }
            if (operationLog.getUserId() == null || operationLog.getUsername() == null) {
                try {
                    if (SecurityUtils.isAuthenticated()) {
                        CustomUserDetails currentUser2 = SecurityUtils.getCurrentUser();
                        if (operationLog.getUserId() == null) operationLog.setUserId(currentUser2.getId());
                        if (operationLog.getUsername() == null) operationLog.setUsername(currentUser2.getUsername());
                    }
                } catch (Exception ignored) {
                }
            }

            return result;
        } catch (Throwable e) {
            operationLog.setStatus("FAIL");
            operationLog.setErrorMessage(StrUtil.maxLength(e.getMessage(), 500));
            throw e;
        } finally {
            // 计算执行时长
            long executeTime = System.currentTimeMillis() - startTime;
            operationLog.setExecuteTime(executeTime);
            operationLog.setCreatedAt(LocalDateTime.now());

            // 异步保存日志
            operationLogService.saveLog(operationLog);
        }
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }

        // 过滤掉ServletRequest、ServletResponse、MultipartFile等对象
        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }

            // 使用 instanceof 过滤不需要序列化的对象
            if (arg instanceof ServletRequest ||
                    arg instanceof ServletResponse ||
                    arg instanceof MultipartFile) {
                continue;
            }

            String argStr = JSONUtil.toJsonStr(arg);
            // 脱敏处理
            argStr = SensitiveDataUtils.maskPasswordInJson(argStr);
            params.append(argStr).append(" ");
        }

        return params.toString().trim();
    }

    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
