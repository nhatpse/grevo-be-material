package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.dto.request.CollectorProfileRequest;
import org.grevo.grevobematerial.dto.response.CollectorProfileResponse;
import org.grevo.grevobematerial.entity.Collectors;
import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.repository.CollectorsRepository;
import org.grevo.grevobematerial.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/collector/profile")
public class CollectorProfileController {

    private final UserRepository userRepository;
    private final CollectorsRepository collectorsRepository;

    public CollectorProfileController(UserRepository userRepository,
            CollectorsRepository collectorsRepository) {
        this.userRepository = userRepository;
        this.collectorsRepository = collectorsRepository;
    }

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private CollectorProfileResponse toResponse(Collectors collector) {
        Users user = collector.getUser();
        Enterprise enterprise = collector.getEnterprise();

        return CollectorProfileResponse.builder()
                .collectorId(collector.getCollectorId())
                .vehicleType(collector.getVehicleType())
                .vehiclePlate(collector.getVehiclePlate())
                .maxCapacity(collector.getMaxCapacity())
                .currentStatus(collector.getCurrentStatus())
                .isOnline(collector.getIsOnline() != null ? collector.getIsOnline() : false)
                .lastActiveAt(collector.getLastActiveAt())
                .user(CollectorProfileResponse.UserInfo.builder()
                        .id(user.getUserId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .build())
                .enterprise(enterprise != null ? CollectorProfileResponse.EnterpriseInfo.builder()
                        .id(enterprise.getEnterpriseId())
                        .companyName(enterprise.getCompanyName())
                        .build() : null)
                .build();
    }

    @GetMapping
    public ResponseEntity<?> getProfile() {
        Users user = getCurrentUser();

        Collectors collector = collectorsRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Collector profile not found for this user"));

        return ResponseEntity.ok(toResponse(collector));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody CollectorProfileRequest request) {
        Users user = getCurrentUser();

        Collectors collector = collectorsRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Collector profile not found for this user"));

        if (request.getVehicleType() != null) {
            collector.setVehicleType(request.getVehicleType());
        }
        if (request.getVehiclePlate() != null) {
            collector.setVehiclePlate(request.getVehiclePlate());
        }
        if (request.getMaxCapacity() != null) {
            collector.setMaxCapacity(request.getMaxCapacity());
        }

        Collectors updated = collectorsRepository.save(collector);

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "data", toResponse(updated)));
    }

    @PostMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestBody Map<String, Boolean> statusUpdate) {
        Users user = getCurrentUser();
        Collectors collector = collectorsRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Collector profile not found"));

        if (!"ACTIVE".equals(collector.getCurrentStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only active collectors can change online status"));
        }

        Boolean isOnline = statusUpdate.get("isOnline");
        if (isOnline == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "isOnline field is required"));
        }

        collector.setIsOnline(isOnline);
        if (Boolean.TRUE.equals(isOnline)) {
            collector.setLastActiveAt(java.time.LocalDateTime.now());
        }
        collectorsRepository.save(collector);

        return ResponseEntity.ok(Map.of(
                "message", "Status updated successfully",
                "isOnline", isOnline));
    }
}
