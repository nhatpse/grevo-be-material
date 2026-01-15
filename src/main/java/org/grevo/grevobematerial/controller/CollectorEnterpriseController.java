package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.entity.Collectors;
import org.grevo.grevobematerial.entity.CollectorRequest;
import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.entity.enums.RequestStatus;
import org.grevo.grevobematerial.repository.CollectorsRepository;
import org.grevo.grevobematerial.repository.CollectorRequestRepository;
import org.grevo.grevobematerial.repository.EnterpriseRepository;
import org.grevo.grevobematerial.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/collector/enterprise")
public class CollectorEnterpriseController {

    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final CollectorRequestRepository collectorRequestRepository;
    private final CollectorsRepository collectorsRepository;

    public CollectorEnterpriseController(UserRepository userRepository,
            EnterpriseRepository enterpriseRepository,
            CollectorRequestRepository collectorRequestRepository,
            CollectorsRepository collectorsRepository) {
        this.userRepository = userRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.collectorRequestRepository = collectorRequestRepository;
        this.collectorsRepository = collectorsRepository;
    }

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private Collectors getOrCreateCollectorProfile(Users user) {
        return collectorsRepository.findByUser(user).orElseGet(() -> {
            Collectors newCollector = new Collectors();
            newCollector.setUser(user);
            return collectorsRepository.save(newCollector);
        });
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        Users user = getCurrentUser();
        Collectors collector = getOrCreateCollectorProfile(user);

        if (collector.getEnterprise() != null) {
            return ResponseEntity.ok(Map.of(
                    "status", "APPROVED",
                    "enterprise", collector.getEnterprise()));
        }

        Optional<CollectorRequest> pendingRequest = collectorRequestRepository.findByUserAndStatus(user,
                RequestStatus.PENDING);
        if (pendingRequest.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", "PENDING",
                    "enterprise", pendingRequest.get().getEnterprise()));
        }

        return ResponseEntity.ok(Map.of("status", "NONE"));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Enterprise>> searchEnterprises(@RequestParam(required = false) String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Enterprise> all = enterpriseRepository.findAll();
        List<Enterprise> filtered = all.stream()
                .filter(e -> (e.getCompanyName() != null
                        && e.getCompanyName().toLowerCase().contains(query.toLowerCase())) ||
                        (e.getTaxCode() != null && e.getTaxCode().contains(query)))
                .limit(20)
                .toList();

        return ResponseEntity.ok(filtered);
    }

    @PostMapping("/join")
    public ResponseEntity<?> requestToJoin(
            @RequestBody org.grevo.grevobematerial.dto.request.CollectorJoinRequest body) {
        Integer enterpriseId = body.getEnterpriseId();
        if (enterpriseId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "enterpriseId is required"));
        }

        Users user = getCurrentUser();
        Collectors collector = getOrCreateCollectorProfile(user);

        if (collector.getEnterprise() != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already belongs to an enterprise"));
        }

        if (collectorRequestRepository.existsByUserAndStatus(user, RequestStatus.PENDING)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already has a pending request"));
        }

        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise not found"));

        CollectorRequest request = new CollectorRequest();
        request.setUser(user);
        request.setEnterprise(enterprise);
        request.setStatus(RequestStatus.PENDING);
        collectorRequestRepository.save(request);

        return ResponseEntity.ok(Map.of("message", "Request sent successfully", "status", "PENDING"));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveOrCancel() {
        Users user = getCurrentUser();
        Collectors collector = getOrCreateCollectorProfile(user);

        if (collector.getEnterprise() != null) {
            collector.setEnterprise(null);
            collectorsRepository.save(collector);
            return ResponseEntity.ok(Map.of("message", "You have left the enterprise", "status", "NONE"));
        }

        Optional<CollectorRequest> pendingRequest = collectorRequestRepository.findByUserAndStatus(user,
                RequestStatus.PENDING);
        if (pendingRequest.isPresent()) {
            collectorRequestRepository.delete(pendingRequest.get());
            return ResponseEntity.ok(Map.of("message", "Request cancelled successfully", "status", "NONE"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "No active enterprise or pending request found"));
    }
}
