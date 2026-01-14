package org.grevo.grevobematerial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grevo.grevobematerial.entity.enums.Role;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagementResponse {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private Boolean isActive;
    private String avatar;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
