package org.max.authvs.api.dto.client.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户端密钥重置结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "客户端密钥重置结果")
public class ClientSecretResetVo {

    @Schema(description = "客户端标识", example = "web-portal")
    private String clientId;

    @Schema(description = "新的客户端密钥（明文，仅返回一次）", example = "abc123def456")
    private String newClientSecret;

    @Schema(description = "提示信息", example = "密钥已重置，请妥善保存，此密钥仅显示一次")
    private String message;
}
