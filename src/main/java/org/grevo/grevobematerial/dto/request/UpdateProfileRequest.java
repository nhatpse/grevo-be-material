package org.grevo.grevobematerial.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String phone;
    private String address;
}
