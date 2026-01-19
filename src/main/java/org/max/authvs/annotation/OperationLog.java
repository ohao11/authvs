package org.max.authvs.annotation;

import org.max.authvs.enums.OperationType;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标记在需要记录操作日志的方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作类型
     */
    OperationType type();

    /**
     * 操作模块
     */
    String module();

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录返回结果
     */
    boolean recordResult() default false;
}
