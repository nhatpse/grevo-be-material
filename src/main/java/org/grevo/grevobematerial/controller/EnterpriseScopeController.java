package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.EnterpriseArea;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.repository.EnterpriseRepository;
import org.grevo.grevobematerial.repository.UserRepository;
import org.grevo.grevobematerial.service.EnterpriseAreaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enterprises/me/scope")
public class EnterpriseScopeController {

    private final EnterpriseAreaService enterpriseAreaService;
    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;

    public EnterpriseScopeController(EnterpriseAreaService enterpriseAreaService,
            EnterpriseRepository enterpriseRepository,
            UserRepository userRepository) {
        this.enterpriseAreaService = enterpriseAreaService;
        this.enterpriseRepository = enterpriseRepository;
        this.userRepository = userRepository;
    }

    private Enterprise getCurrentEnterprise() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return enterpriseRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException(
                        "Current user is not an Enterprise or Enterprise profile not found"));
    }

    @GetMapping
    public ResponseEntity<?> getMyScope() {
        try {
            Enterprise enterprise = getCurrentEnterprise();
            return ResponseEntity.ok(enterpriseAreaService.getAreas(enterprise.getEnterpriseId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> addToScope(
            @RequestBody org.grevo.grevobematerial.dto.request.EnterpriseScopeRequest body) {
        Integer areaId = body.getAreaId();
        if (areaId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "areaId is missing"));
        }

        try {
            Enterprise enterprise = getCurrentEnterprise();
            EnterpriseArea area = enterpriseAreaService.addAreaById(enterprise.getEnterpriseId(), areaId);
            return ResponseEntity.ok(area);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{areaId}")
    public ResponseEntity<?> removeFromScope(@PathVariable Integer areaId) {
        try {
            Enterprise enterprise = getCurrentEnterprise();
            enterpriseAreaService.removeArea(enterprise.getEnterpriseId(), areaId);
            return ResponseEntity.ok(Map.of("message", "Area removed from scope successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
