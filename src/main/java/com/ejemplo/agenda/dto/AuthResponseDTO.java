package com.ejemplo.agenda.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, String tokenType, Long userId, String username, String email, String role, Long expiresIn) {
        this.token = token;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long userId;
    private String username;
    private String email;
    private String role;
    private Long expiresIn;
}
