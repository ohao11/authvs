package org.max.authvs.api.dto.client.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 客户端保存请求参数（创建/更新）
 */
@Data
@Schema(description = "客户端保存参数")
public class ClientSaveParam {

    @Schema(description = "客户端名称", example = "Web门户应用", required = true)
    @NotBlank(message = "客户端名称不能为空")
    private String clientName;

    @Schema(description = "客户端类型：1-Web应用 2-移动应用 3-单页应用 4-服务端应用", example = "1", required = true)
    @NotNull(message = "客户端类型不能为空")
    private Integer clientType;

    @Schema(description = "授权类型（多个用逗号分隔）", example = "authorization_code,refresh_token", required = true)
    @NotBlank(message = "授权类型不能为空")
    private String grantTypes;

    @Schema(description = "重定向URI（多个用逗号分隔）", example = "http://localhost:3000/callback,https://portal.example.com/callback")
    private String redirectUris;

    @Schema(description = "登出后重定向URI（多个用逗号分隔）", example = "http://localhost:3000,https://portal.example.com")
    private String postLogoutRedirectUris;

    @Schema(description = "Access Token有效期（秒）", example = "3600")
    private Integer accessTokenValidity = 3600;

    @Schema(description = "Refresh Token有效期（秒）", example = "2592000")
    private Integer refreshTokenValidity = 2592000;

    @Schema(description = "ID Token有效期（秒）", example = "3600")
    private Integer idTokenValidity = 3600;

    @Schema(description = "允许的作用域（多个用逗号分隔）", example = "openid,profile,email")
    private String scopes;

    @Schema(description = "是否自动授权", example = "false")
    private Boolean autoApprove = false;

    @Schema(description = "客户端描述", example = "Web门户前端应用")
    private String description;

    @Schema(description = "客户端Logo URL", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "客户端主页URL", example = "https://example.com")
    private String homeUrl;
}
