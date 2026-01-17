package org.max.authvs.api;

import org.max.authvs.api.dto.user.out.UserProfile;
import org.max.authvs.api.dto.ResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户", description = "用户相关接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Operation(summary = "获取当前用户", description = "获取当前认证用户的个人信息")
    @GetMapping("/me")
    public ResultDTO<UserProfile> me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return ResultDTO.success(new UserProfile(authentication.getName(), roles));
    }
}