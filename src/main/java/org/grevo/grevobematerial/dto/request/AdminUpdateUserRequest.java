package org.grevo.grevobematerial.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grevo.grevobematerial.entity.enums.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {
    private Role role;
    private Boolean isActive;
}
