package org.max.authvs.security;

import lombok.extern.slf4j.Slf4j;
import org.max.authvs.service.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 自定义认证提供者：登录时禁用缓存，确保密码验证正确
 * <p>
 * 在加载用户详情时禁用缓存，强制从数据库查询用户信息包括密码，
 * 以确保密码验证的准确性。
 */
@Slf4j
public class NoCacheAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public NoCacheAuthenticationProvider(CustomUserDetailsService customUserDetailsService,
                                         PasswordEncoder passwordEncoder) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String presentedPassword = (String) authentication.getCredentials();

        log.debug("Attempting to authenticate user: {}", username);

        // 关键：禁用缓存，直接从数据库加载用户信息（包括密码）用于认证
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username, false);

        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            log.debug("Failed to authenticate user: {} - password mismatch", username);
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!userDetails.isEnabled()) {
            log.debug("Failed to authenticate user: {} - account disabled", username);
            throw new BadCredentialsException("User account is disabled");
        }

        log.debug("Successfully authenticated user: {}", username);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                presentedPassword,
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
