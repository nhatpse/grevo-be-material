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
            String status = "APPROVED";

            Optional<CollectorRequest> leaveReq = collectorRequestRepository.findByUserAndStatusAndType(user,
                    RequestStatus.PENDING, org.grevo.grevobematerial.entity.enums.RequestType.LEAVE);

            if (leaveReq.isPresent()) {
                status = "LEAVE_REQUESTED";
            } else {
                Optional<CollectorRequest> leaveRej = collectorRequestRepository.findByUserAndStatusAndType(user,
                        RequestStatus.REJECTED, org.grevo.grevobematerial.entity.enums.RequestType.LEAVE);
                if (leaveRej.isPresent()) {
                    status = "LEAVE_REJECTED";
                }
            }

            return ResponseEntity.ok(Map.of(
                    "status", status,
                    "enterprise", collector.getEnterprise()));
        }

        Optional<CollectorRequest> pendingRequest = collectorRequestRepository.findByUserAndStatusAndType(user,
                RequestStatus.PENDING, org.grevo.grevobematerial.entity.enums.RequestType.JOIN);
        if (pendingRequest.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", "PENDING",
                    "enterprise", pendingRequest.get().getEnterprise()));
        }

        Optional<CollectorRequest> rejectedRequest = collectorRequestRepository.findByUserAndStatusAndType(user,
                RequestStatus.REJECTED, org.grevo.grevobematerial.entity.enums.RequestType.JOIN);
        if (rejectedRequest.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", "REJECTED",
                    "enterprise", rejectedRequest.get().getEnterprise()));
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

        if (collectorRequestRepository.existsByUserAndStatusAndType(user, RequestStatus.PENDING,
                org.grevo.grevobematerial.entity.enums.RequestType.JOIN)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already has a pending join request"));
        }

        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise not found"));

        List<CollectorRequest> existingRequests = collectorRequestRepository.findAllByUser(user);
        CollectorRequest request;
        if (!existingRequests.isEmpty()) {
            // Use the first one
            request = existingRequests.get(0);

            // Delete duplicates
            if (existingRequests.size() > 1) {
                for (int i = 1; i < existingRequests.size(); i++) {
                    collectorRequestRepository.delete(existingRequests.get(i));
                }
            }

            request.setEnterprise(enterprise);
            request.setStatus(RequestStatus.PENDING);
            request.setType(org.grevo.grevobematerial.entity.enums.RequestType.JOIN);
        } else {
            request = new CollectorRequest();
            request.setUser(user);
            request.setEnterprise(enterprise);
            request.setStatus(RequestStatus.PENDING);
            request.setType(org.grevo.grevobematerial.entity.enums.RequestType.JOIN);
        }

        collectorRequestRepository.save(request);

        return ResponseEntity.ok(Map.of("message", "Request sent successfully", "status", "PENDING"));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveOrCancel(@RequestBody(required = false) Map<String, String> body) {
        Users user = getCurrentUser();
        Collectors collector = getOrCreateCollectorProfile(user);

        if (collector.getEnterprise() != null) {
            Optional<CollectorRequest> leaveReq = collectorRequestRepository.findByUserAndStatusAndType(user,
                    RequestStatus.PENDING, org.grevo.grevobematerial.entity.enums.RequestType.LEAVE);

            if (leaveReq.isPresent()) {
                collectorRequestRepository.delete(leaveReq.get());
                collector.setCurrentStatus("ACTIVE");
                collector.setLeaveReason(null);
                collectorsRepository.save(collector);

                return ResponseEntity.ok(Map.of("message", "Leave request cancelled.", "status", "APPROVED"));
            }

            Optional<CollectorRequest> leaveRej = collectorRequestRepository.findByUserAndStatusAndType(user,
                    RequestStatus.REJECTED, org.grevo.grevobematerial.entity.enums.RequestType.LEAVE);

            if (leaveRej.isPresent()) {
                collectorRequestRepository.delete(leaveRej.get());
                collector.setCurrentStatus("ACTIVE");
                collector.setLeaveReason(null);
                collectorsRepository.save(collector);

                return ResponseEntity.ok(Map.of("message", "Notification acknowledged.", "status", "APPROVED"));
            }

            String reason = (body != null) ? body.get("reason") : null;
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Reason is required to leave enterprise"));
            }

            CollectorRequest req = new CollectorRequest();
            req.setUser(user);
            req.setEnterprise(collector.getEnterprise());
            req.setStatus(RequestStatus.PENDING);
            req.setType(org.grevo.grevobematerial.entity.enums.RequestType.LEAVE);
            req.setReason(reason);
            collectorRequestRepository.save(req);

            collector.setCurrentStatus("LEAVE_REQUESTED");
            collector.setLeaveReason(reason);
            collectorsRepository.save(collector);

            return ResponseEntity
                    .ok(Map.of("message", "Leave request sent. Waiting for approval.", "status", "LEAVE_REQUESTED"));
        }

        Optional<CollectorRequest> pendingRequest = collectorRequestRepository.findByUserAndStatusAndType(user,
                RequestStatus.PENDING, org.grevo.grevobematerial.entity.enums.RequestType.JOIN);
        if (pendingRequest.isPresent()) {
            collectorRequestRepository.delete(pendingRequest.get());
            return ResponseEntity.ok(Map.of("message", "Request cancelled successfully", "status", "NONE"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "No active enterprise or pending request found"));
    }
}
