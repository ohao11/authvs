package org.max.authvs.api.dto.user.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.max.authvs.api.dto.PageQuery;

/**
 * 用户查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户查询参数")
public class UserQueryParam extends PageQuery {

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "是否启用：0-禁用 1-启用")
    private Integer enabled;
}
