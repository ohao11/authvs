package org.max.authvs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 异步任务配置 - 使用 Java 21 虚拟线程
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 操作日志异步执行器 - 基于虚拟线程
     * Java 21 虚拟线程优势：
     * 1. 轻量级：可创建数百万个虚拟线程，内存占用极小
     * 2. 无需调优：无需配置核心线程数、最大线程数、队列容量
     * 3. 自动伸缩：根据负载自动创建和销毁
     * 4. 简化管理：无需担心线程池耗尽或队列溢出
     */
    @Bean("operationLogExecutor")
    public Executor operationLogExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
