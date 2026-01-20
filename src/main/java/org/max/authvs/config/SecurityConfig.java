package org.max.authvs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.security.JwtAuthenticationFilter;
import org.max.authvs.security.NoCacheAuthenticationProvider;
import org.max.authvs.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(ObjectMapper objectMapper,
                          CustomUserDetailsService customUserDetailsService) {
        this.objectMapper = objectMapper;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, HttpStatus.UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, HttpStatus.FORBIDDEN, "Forbidden"))
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider noCacheAuthenticationProvider(PasswordEncoder passwordEncoder) {
        return new NoCacheAuthenticationProvider(customUserDetailsService, passwordEncoder);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        int code = status.value();
        ResultDTO<Void> body = ResultDTO.error(code, message);
        objectMapper.writeValue(response.getWriter(), body);
    }
}