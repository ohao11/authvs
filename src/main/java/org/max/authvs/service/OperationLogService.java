package org.max.authvs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.max.authvs.api.dto.log.OperationLogQueryParam;
import org.max.authvs.api.dto.log.out.OperationLogVo;
import org.max.authvs.entity.OperationLog;
import org.max.authvs.mapper.OperationLogMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务
 */
@Slf4j
@Service
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    /**
     * 异步保存操作日志
     */
    @Async("operationLogExecutor")
    public void saveLog(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 分页查询操作日志
     */
    public Page<OperationLogVo> getOperationLogs(OperationLogQueryParam param) {
        Page<OperationLog> page = new Page<>(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(param.getUsername()), OperationLog::getUsername, param.getUsername())
                .eq(StringUtils.hasText(param.getOperationType()), OperationLog::getOperationType, param.getOperationType())
                .eq(StringUtils.hasText(param.getOperationModule()), OperationLog::getOperationModule, param.getOperationModule())
                .eq(StringUtils.hasText(param.getStatus()), OperationLog::getStatus, param.getStatus())
                .eq(StringUtils.hasText(param.getIpAddress()), OperationLog::getIpAddress, param.getIpAddress())
                .ge(param.getStartTime() != null, OperationLog::getCreatedAt, param.getStartTime())
                .le(param.getEndTime() != null, OperationLog::getCreatedAt, param.getEndTime())
                .orderByDesc(OperationLog::getCreatedAt);

        Page<OperationLog> resultPage = operationLogMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<OperationLogVo> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<OperationLogVo> voList = resultPage.getRecords().stream()
                .map(this::convertToVo)
                .toList();
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 根据ID查询操作日志详情
     */
    public OperationLogVo getOperationLogById(Long id) {
        OperationLog operationLog = operationLogMapper.selectById(id);
        if (operationLog == null) {
            return null;
        }
        return convertToVo(operationLog);
    }

    /**
     * 归档超过6个月的日志
     * 实际项目中可通过定时任务调用
     */
    public int archiveOldLogs() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperationLog::getCreatedAt, sixMonthsAgo);

        // 这里简单删除，实际项目中可以先备份到归档表
        int count = operationLogMapper.delete(wrapper);
        log.info("归档并删除了 {} 条超过6个月的操作日志", count);
        return count;
    }

    /**
     * 转换为VO对象
     */
    private OperationLogVo convertToVo(OperationLog operationLog) {
        return new OperationLogVo(
            operationLog.getId(),
            operationLog.getUserId(),
            operationLog.getUsername(),
            operationLog.getOperationType(),
            operationLog.getOperationModule(),
            operationLog.getOperationDesc(),
            operationLog.getRequestMethod(),
            operationLog.getRequestUrl(),
            operationLog.getRequestParams(),
            operationLog.getResponseResult(),
            operationLog.getIpAddress(),
            operationLog.getDeviceType(),
            operationLog.getUserAgent(),
            operationLog.getPlatformType(),
            operationLog.getExecuteTime(),
            operationLog.getStatus(),
            operationLog.getErrorMessage(),
            operationLog.getCreatedAt()
        );
    }
}
