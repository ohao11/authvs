package org.max.authvs.api.dto.client.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户端响应信息
 */
@Data
@Schema(description = "客户端信息")
public class ClientVo {

    @Schema(description = "客户端ID", example = "1")
    private Long id;

    @Schema(description = "客户端标识", example = "web-portal")
    private String clientId;

    @Schema(description = "客户端密钥（脱敏显示）", example = "$2a$****1Jm")
    private String clientSecret;

    @Schema(description = "客户端名称", example = "Web门户应用")
    private String clientName;

    @Schema(description = "授权类型列表", example = "[\"authorization_code\", \"refresh_token\"]")
    private List<String> grantTypes;

    @Schema(description = "重定向URI列表", example = "[\"http://localhost:3000/callback\"]")
    private List<String> redirectUris;

    @Schema(description = "登出后重定向URI列表", example = "[\"http://localhost:3000\"]")
    private List<String> postLogoutRedirectUris;

    @Schema(description = "Access Token有效期（秒）", example = "3600")
    private Integer accessTokenValidity;

    @Schema(description = "Refresh Token有效期（秒）", example = "2592000")
    private Integer refreshTokenValidity;

    @Schema(description = "ID Token有效期（秒）", example = "3600")
    private Integer idTokenValidity;

    @Schema(description = "作用域列表", example = "[\"openid\", \"profile\", \"email\"]")
    private List<String> scopes;

    @Schema(description = "是否自动授权", example = "false")
    private Boolean autoApprove;

    @Schema(description = "客户端类型：1-Web应用 2-移动应用 3-单页应用 4-服务端应用", example = "1")
    private Integer clientType;

    @Schema(description = "客户端类型描述", example = "Web应用")
    private String clientTypeDesc;

    @Schema(description = "客户端描述", example = "Web门户前端应用")
    private String description;

    @Schema(description = "客户端Logo URL", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "客户端主页URL", example = "https://example.com")
    private String homeUrl;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间", example = "2026-01-23T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-23T10:00:00")
    private LocalDateTime updatedAt;
}
