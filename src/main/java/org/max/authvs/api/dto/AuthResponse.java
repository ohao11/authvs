package org.max.authvs.api.dto;

import java.util.List;

public record AuthResponse(String username, List<String> roles, String token, String message) {
}