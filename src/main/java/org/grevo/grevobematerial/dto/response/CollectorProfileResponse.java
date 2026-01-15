package org.grevo.grevobematerial.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CollectorProfileResponse {
    private Integer collectorId;
    private String vehicleType;
    private String vehiclePlate;
    private Integer maxCapacity;
    private String currentStatus;
    private LocalDateTime lastActiveAt;
    private UserInfo user;
    private EnterpriseInfo enterprise;

    @Data
    @Builder
    public static class UserInfo {
        private Integer id;
        private String fullName;
        private String email;
    }

    @Data
    @Builder
    public static class EnterpriseInfo {
        private Integer id;
        private String companyName;
    }
}
