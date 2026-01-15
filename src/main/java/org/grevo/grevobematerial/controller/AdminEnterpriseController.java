package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.repository.EnterpriseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/enterprises")
public class AdminEnterpriseController {

    private final EnterpriseRepository enterpriseRepository;

    public AdminEnterpriseController(EnterpriseRepository enterpriseRepository) {
        this.enterpriseRepository = enterpriseRepository;
    }

    @GetMapping
    public ResponseEntity<List<Enterprise>> getAllEnterprises() {
        return ResponseEntity.ok(enterpriseRepository.findAll());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Boolean> body) {
        Boolean isActive = body.get("isActive");
        if (isActive == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "isActive is required"));
        }

        Enterprise enterprise = enterpriseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enterprise not found"));

        enterprise.setIsActive(isActive);
        enterpriseRepository.save(enterprise);

        return ResponseEntity.ok(Map.of("message", "Enterprise status updated successfully", "isActive", isActive));
    }
}
