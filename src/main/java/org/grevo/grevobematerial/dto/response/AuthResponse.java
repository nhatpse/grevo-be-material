package org.grevo.grevobematerial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String message;

    public AuthResponse(String token, Integer userId, String username, String fullName, String email, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public AuthResponse(String message) {
        this.message = message;
    }

}