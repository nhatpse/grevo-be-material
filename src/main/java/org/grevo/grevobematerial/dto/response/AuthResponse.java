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
    private String googleId;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String role;
    private String avatar;
    private String message;

    public AuthResponse(String token, Integer userId, String username, String googleId, String fullName,
            String email, String phone, String address, String role, String avatar) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.googleId = googleId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.avatar = avatar;
    }

    public AuthResponse(String message) {
        this.message = message;
    }

}