package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.repository.EnterpriseRepository;
import org.grevo.grevobematerial.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enterprises")
public class EnterpriseController {

    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;

    public EnterpriseController(EnterpriseRepository enterpriseRepository, UserRepository userRepository) {
        this.enterpriseRepository = enterpriseRepository;
        this.userRepository = userRepository;
    }

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile() {
        try {
            Users user = getCurrentUser();
            Enterprise enterprise = enterpriseRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Enterprise profile not found for current user"));
            return ResponseEntity.ok(enterprise);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody Enterprise request) {
        try {
            Users user = getCurrentUser();
            Enterprise enterprise = enterpriseRepository.findByUser(user).orElse(new Enterprise());

            if (enterprise.getEnterpriseId() == null) {
                enterprise.setUser(user);
            }

            if (request.getCompanyName() != null)
                enterprise.setCompanyName(request.getCompanyName());
            if (request.getCompanyPhone() != null)
                enterprise.setCompanyPhone(request.getCompanyPhone());
            if (request.getCompanyEmail() != null)
                enterprise.setCompanyEmail(request.getCompanyEmail());
            if (request.getCompanyAdr() != null)
                enterprise.setCompanyAdr(request.getCompanyAdr());
            if (request.getTaxCode() != null)
                enterprise.setTaxCode(request.getTaxCode());

            if (request.getCapacity() != null) {
                if (request.getCapacity() <= 0) {
                    throw new RuntimeException("Capacity must be a positive integer");
                }
                enterprise.setCapacity(request.getCapacity());
            }

            return ResponseEntity.ok(enterpriseRepository.save(enterprise));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
