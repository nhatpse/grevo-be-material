package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.entity.CollectorRequest;
import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.entity.enums.RequestStatus;
import org.grevo.grevobematerial.repository.CollectorRequestRepository;
import org.grevo.grevobematerial.repository.CollectorsRepository;
import org.grevo.grevobematerial.repository.EnterpriseRepository;
import org.grevo.grevobematerial.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/enterprises/me/collectors")
public class EnterpriseCollectorController {

    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final CollectorsRepository collectorsRepository;
    private final CollectorRequestRepository collectorRequestRepository;

    public EnterpriseCollectorController(UserRepository userRepository,
            EnterpriseRepository enterpriseRepository,
            CollectorsRepository collectorsRepository,
            CollectorRequestRepository collectorRequestRepository) {
        this.userRepository = userRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.collectorsRepository = collectorsRepository;
        this.collectorRequestRepository = collectorRequestRepository;
    }

    private Enterprise getCurrentEnterprise() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return enterpriseRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Current user is not an enterprise owner"));
    }

    @GetMapping
    public ResponseEntity<?> getCollectors(@RequestParam(defaultValue = "APPROVED") String status) {
        Enterprise enterprise = getCurrentEnterprise();

        if ("PENDING".equalsIgnoreCase(status)) {
            List<CollectorRequest> requests = collectorRequestRepository.findByEnterpriseAndStatus(enterprise,
                    RequestStatus.PENDING);
            var result = requests.stream().map(req -> {
                Users u = req.getUser();
                Optional<org.grevo.grevobematerial.entity.Collectors> c = collectorsRepository.findByUser(u);
                Integer collectorId = c.map(org.grevo.grevobematerial.entity.Collectors::getCollectorId).orElse(null);

                java.util.HashMap<String, Object> item = new java.util.HashMap<>();
                item.put("id", collectorId);
                item.put("requestId", req.getId());
                item.put("collectorId", collectorId);
                item.put("userId", u.getUserId());
                item.put("fullName", u.getFullName() != null ? u.getFullName() : "");
                item.put("email", u.getEmail() != null ? u.getEmail() : "");
                item.put("phone", u.getPhone() != null ? u.getPhone() : "");
                item.put("status", "PENDING");
                item.put("requestDate", req.getCreatedAt());
                return item;
            }).toList();
            return ResponseEntity.ok(result);
        } else {
            List<org.grevo.grevobematerial.entity.Collectors> list = collectorsRepository.findByEnterprise(enterprise);
            return ResponseEntity.ok(list);
        }
    }

    @PostMapping("/{collectorId}/approve")
    public ResponseEntity<?> approveCollector(@PathVariable Integer collectorId) {
        Enterprise enterprise = getCurrentEnterprise();

        org.grevo.grevobematerial.entity.Collectors collector = collectorsRepository.findById(collectorId)
                .orElseThrow(() -> new RuntimeException("Collector not found"));

        CollectorRequest request = collectorRequestRepository
                .findByUserAndStatus(collector.getUser(), RequestStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("No pending request found for this collector"));

        if (!request.getEnterprise().getEnterpriseId().equals(enterprise.getEnterpriseId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Request does not belong to this enterprise"));
        }

        collector.setEnterprise(enterprise);
        collectorsRepository.save(collector);
        collectorRequestRepository.delete(request);

        return ResponseEntity.ok(Map.of("message", "Collector approved successfully"));
    }

    @PostMapping("/{collectorId}/reject")
    public ResponseEntity<?> rejectOrRemoveCollector(@PathVariable Integer collectorId) {
        Enterprise enterprise = getCurrentEnterprise();

        org.grevo.grevobematerial.entity.Collectors collector = collectorsRepository.findById(collectorId)
                .orElseThrow(() -> new RuntimeException("Collector not found"));

        boolean isRemoved = false;

        Optional<CollectorRequest> requestOpt = collectorRequestRepository.findByUserAndStatus(collector.getUser(),
                RequestStatus.PENDING);
        if (requestOpt.isPresent()) {
            CollectorRequest request = requestOpt.get();
            if (request.getEnterprise().getEnterpriseId().equals(enterprise.getEnterpriseId())) {
                collectorRequestRepository.delete(request);
                isRemoved = true;
            }
        }

        if (collector.getEnterprise() != null
                && collector.getEnterprise().getEnterpriseId().equals(enterprise.getEnterpriseId())) {
            collector.setEnterprise(null);
            collectorsRepository.save(collector);
            isRemoved = true;
        }

        if (isRemoved) {
            return ResponseEntity.ok(Map.of("message", "Collector rejected/removed successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Collector is not associated with this enterprise"));
        }
    }
}
