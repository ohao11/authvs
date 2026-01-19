package org.max.authvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作类型：LOGIN LOGOUT CREATE UPDATE DELETE QUERY EXPORT IMPORT
     */
    private String operationType;

    /**
     * 操作模块
     */
    private String operationModule;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 请求方法：GET POST PUT DELETE
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;

    /**
     * 返回结果（JSON格式）
     */
    private String responseResult;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 设备类型：WEB IOS ANDROID PC
     */
    private String deviceType;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 执行时长（毫秒）
     */
    private Long executeTime;

    /**
     * 执行状态：SUCCESS FAIL
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
