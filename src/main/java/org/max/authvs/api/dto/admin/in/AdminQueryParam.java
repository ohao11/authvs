package org.max.authvs.api.dto.admin.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.max.authvs.api.dto.PageQuery;

/**
 * 管理员查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理员查询参数")
public class AdminQueryParam extends PageQuery {

    @Schema(description = "用户名（模糊查询）", example = "admin")
    private String username;

    @Schema(description = "邮箱（模糊查询）", example = "admin@example.com")
    private String email;

    @Pattern(regexp = "^(1[3-9]\\d{9})?$", message = "手机号格式不正确")
    @Schema(description = "手机号（精确查询）", example = "13800138000")
    private String phone;

    @Schema(description = "是否启用：0-禁用 1-启用")
    private Integer enabled;
}
