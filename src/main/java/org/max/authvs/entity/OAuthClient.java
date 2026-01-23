package org.max.authvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OIDC客户端实体类，对应数据库oauth_clients表（MyBatis-Plus）
 */
@TableName("oauth_clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClient {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String clientId;
    private String clientSecret;
    private String clientName;

    // 授权相关
    private String grantTypes;
    private String redirectUris;
    private String postLogoutRedirectUris;

    // Token配置
    private Integer accessTokenValidity;
    private Integer refreshTokenValidity;
    private Integer idTokenValidity;

    // 作用域和权限
    private String scopes;
    private Boolean autoApprove;

    // 客户端信息
    private Integer clientType;
    private String description;
    private String logoUrl;
    private String homeUrl;

    // 状态管理
    private Boolean enabled;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
