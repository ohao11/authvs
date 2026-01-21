package org.max.authvs.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.max.authvs.annotation.OperationLog;
import org.max.authvs.api.dto.PageVo;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.api.dto.log.OperationLogQueryParam;
import org.max.authvs.api.dto.log.out.OperationLogVo;
import org.max.authvs.enums.OperationType;
import org.max.authvs.service.OperationLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志", description = "操作日志管理相关接口")
@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @OperationLog(type = OperationType.QUERY, module = "审计管理", description = "分页查询审计日志")
    @Operation(summary = "分页查询审计日志", description = "支持多条件查询（用户名、操作类型、模块、状态、平台类型等），可通过platformType参数区分后台管理和门户日志，需要审计列表权限")
    @PreAuthorize("@accessChecker.perm('AUDIT_LIST')")
    @PostMapping("/page")
    public ResultDTO<PageVo<OperationLogVo>> getOperationLogs(@RequestBody OperationLogQueryParam param) {
        Page<OperationLogVo> page = operationLogService.getOperationLogs(param);
        PageVo<OperationLogVo> pageVo = PageVo.<OperationLogVo>builder()
                .total(page.getTotal())
                .pageSize(page.getSize())
                .pageNum(page.getCurrent())
                .records(page.getRecords())
                .build();
        return ResultDTO.success(pageVo);
    }

    @OperationLog(type = OperationType.QUERY, module = "审计管理", description = "查询审计日志详情")
    @Operation(summary = "查询审计日志详情", description = "根据ID查询日志详情，需要审计列表权限")
    @PreAuthorize("@accessChecker.perm('AUDIT_LIST')")
    @GetMapping("/{id}")
    public ResultDTO<OperationLogVo> getOperationLogById(@PathVariable Long id) {
        OperationLogVo vo = operationLogService.getOperationLogById(id);
        return ResultDTO.success(vo);
    }

    @OperationLog(type = OperationType.DELETE, module = "审计管理", description = "归档旧日志")
    @Operation(summary = "归档旧日志", description = "归档并删除超过6个月的日志，需要审计列表权限")
    @PreAuthorize("@accessChecker.perm('AUDIT_LIST')")
    @PostMapping("/archive")
    public ResultDTO<Integer> archiveOldLogs() {
        int count = operationLogService.archiveOldLogs();
        return ResultDTO.success(count);
    }
}
