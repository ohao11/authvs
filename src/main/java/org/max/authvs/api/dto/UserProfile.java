package org.max.authvs.api.dto;

import java.util.List;

public record UserProfile(String username, List<String> roles) {
}