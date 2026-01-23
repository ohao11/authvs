package org.max.authvs.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.max.authvs.annotation.OperationLog;
import org.max.authvs.api.dto.PageVo;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.api.dto.client.in.ClientQueryParam;
import org.max.authvs.api.dto.client.in.ClientSaveParam;
import org.max.authvs.api.dto.client.out.ClientSecretResetVo;
import org.max.authvs.api.dto.client.out.ClientVo;
import org.max.authvs.enums.OperationType;
import org.max.authvs.service.OAuthClientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * OIDC客户端管理接口
 */
@Tag(name = "客户端管理", description = "OIDC客户端管理相关接口")
@RestController
@RequestMapping("/api/admin/clients")
public class OAuthClientController {

    private final OAuthClientService clientService;

    public OAuthClientController(OAuthClientService clientService) {
        this.clientService = clientService;
    }

    @OperationLog(type = OperationType.QUERY, module = "认证管理", description = "分页查询客户端列表")
    @Operation(summary = "分页查询客户端列表", description = "查询OIDC客户端列表，支持按客户端名称、客户端ID、类型、状态等条件查询")
    @PreAuthorize("@accessChecker.perm('CLIENT_LIST')")
    @PostMapping("/page")
    public ResultDTO<PageVo<ClientVo>> getClientsByPage(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "客户端查询参数",
                    required = true)
            @Valid @RequestBody ClientQueryParam queryParam) {
        PageVo<ClientVo> pageVo = clientService.getClientsByPage(queryParam);
        return ResultDTO.success(pageVo);
    }

    @OperationLog(type = OperationType.QUERY, module = "认证管理", description = "查询客户端详情")
    @Operation(summary = "获取客户端详细信息", description = "根据客户端ID查询客户端详细信息（密钥脱敏显示）")
    @PreAuthorize("@accessChecker.perm('CLIENT_LIST')")
    @GetMapping("/{id}")
    public ResultDTO<ClientVo> getClientById(
            @Parameter(description = "客户端ID", example = "1")
            @PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("客户端ID无效");
        }
        ClientVo clientVo = clientService.getClientById(id);
        return ResultDTO.success(clientVo);
    }

    @OperationLog(type = OperationType.CREATE, module = "认证管理", description = "创建客户端")
    @Operation(summary = "创建客户端", description = "创建新的OIDC客户端，自动生成client_id和client_secret（明文密钥仅返回一次）")
    @PreAuthorize("@accessChecker.perm('CLIENT_LIST')")
    @PostMapping
    public ResultDTO<ClientVo> createClient(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "客户端信息",
                    required = true)
            @Valid @RequestBody ClientSaveParam saveParam) {
        ClientVo clientVo = clientService.createClient(saveParam);
        return ResultDTO.success(clientVo);
    }

    @OperationLog(type = OperationType.UPDATE, module = "认证管理", description = "更新客户端信息")
    @Operation(summary = "更新客户端信息", description = "更新客户端配置信息（不包括密钥，密钥需单独重置）")
    @PreAuthorize("@accessChecker.perm('CLIENT_LIST')")
    @PutMapping("/{id}")
    public ResultDTO<ClientVo> updateClient(
            @Parameter(description = "客户端ID", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "客户端信息",
                    required = true)
            @Valid @RequestBody ClientSaveParam saveParam) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("客户端ID无效");
        }
        ClientVo clientVo = clientService.updateClient(id, saveParam);
        return ResultDTO.success(clientVo);
    }

    @OperationLog(type = OperationType.UPDATE, module = "认证管理", description = "切换客户端状态")
    @Operation(summary = "启用/禁用客户端", description = "切换客户端的启用状态")
    @PreAuthorize("@accessChecker.perm('CLIENT_LIST')")
    @PatchMapping("/{id}/status")
    public ResultDTO<Void> toggleClientStatus(
            @Parameter(description = "客户端ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "是否启用", example = "true")
            @RequestParam Boolean enabled) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("客户端ID无效");
        }
        if (enabled == null) {
            throw new IllegalArgumentException("启用状态不能为空");
        }
        clientService.toggleClientStatus(id, enabled);
        return ResultDTO.success(null);
    }

    @OperationLog(type = OperationType.UPDATE, module = "认证管理", description = "重置客户端密钥")
    @Operation(summary = "重置客户端密钥", description = "重新生成客户端密钥（明文密钥仅返回一次，请妥善保存）")
    @PreAuthorize("@accessChecker.perm('CLIENT_LIST')")
    @PostMapping("/{id}/reset-secret")
    public ResultDTO<ClientSecretResetVo> resetClientSecret(
            @Parameter(description = "客户端ID", example = "1")
            @PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("客户端ID无效");
        }
        ClientSecretResetVo resetVo = clientService.resetClientSecret(id);
        return ResultDTO.success(resetVo);
    }
}
