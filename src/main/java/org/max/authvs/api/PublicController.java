package org.max.authvs.api;

import cn.hutool.core.util.DesensitizedUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.max.authvs.annotation.OperationLog;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.api.dto.user.out.UserVo;
import org.max.authvs.enums.OperationType;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.utils.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共接口 - 当前用户信息
 */
@Tag(name = "公共接口", description = "通用接口")
@RestController
@RequestMapping("/api")
public class PublicController {

    public PublicController() {
    }

    @OperationLog(type = OperationType.QUERY, module = "用户管理", description = "查询当前用户信息")
    @Operation(summary = "获取当前用户", description = "获取当前认证用户的个人信息")
    @GetMapping("/me")
    public ResultDTO<UserVo> me() {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUser();
        UserVo userVo = new UserVo(
                userDetails.getId(),
                userDetails.getUsername(),
                DesensitizedUtil.email(userDetails.getEmail()),
                DesensitizedUtil.mobilePhone(userDetails.getPhone()),
                userDetails.getUserType(),
                userDetails.isEnabled()
        );
        return ResultDTO.success(userVo);
    }
}
