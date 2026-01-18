package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.entity.CollectorRequest;
import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.entity.enums.RequestStatus;
import org.grevo.grevobematerial.entity.enums.RequestType;
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
import java.util.Set;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<Map<String, Object>>> getCollectors(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        Enterprise enterprise = getCurrentEnterprise();
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        if ("PENDING".equalsIgnoreCase(status)) {
            List<CollectorRequest> requests = new java.util.ArrayList<>(
                    collectorRequestRepository.findByEnterpriseAndStatusAndType(enterprise,
                            RequestStatus.PENDING, RequestType.JOIN));

            List<CollectorRequest> legacyRequests = collectorRequestRepository
                    .findByEnterpriseAndStatus(enterprise, RequestStatus.PENDING)
                    .stream().filter(r -> r.getType() == null).toList();
            requests.addAll(legacyRequests);

            for (CollectorRequest req : requests) {
                Users u = req.getUser();
                org.grevo.grevobematerial.entity.Collectors c = collectorsRepository.findByUser(u).orElse(null);

                Map<String, Object> item = new java.util.HashMap<>();
                item.put("id", u.getUserId());
                item.put("fullName", u.getFullName() != null ? u.getFullName() : "");
                item.put("email", u.getEmail() != null ? u.getEmail() : "");
                item.put("phone", u.getPhone() != null ? u.getPhone() : "");
                item.put("status", "PENDING");
                item.put("requestDate", req.getCreatedAt());
                item.put("vehicleType", c != null ? c.getVehicleType() : "");
                item.put("vehiclePlate", c != null ? c.getVehiclePlate() : "");

                result.add(item);
            }
        } else {
            List<org.grevo.grevobematerial.entity.Collectors> list = collectorsRepository.findByEnterprise(enterprise);

            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                list = list.stream().filter(c -> (c.getUser().getFullName() != null
                        && c.getUser().getFullName().toLowerCase().contains(searchLower)) ||
                        (c.getUser().getEmail() != null && c.getUser().getEmail().toLowerCase().contains(searchLower))
                        ||
                        (c.getUser().getPhone() != null && c.getUser().getPhone().contains(searchLower))).toList();
            }

            List<CollectorRequest> leaveRequests = collectorRequestRepository.findByEnterpriseAndStatusAndType(
                    enterprise,
                    RequestStatus.PENDING, RequestType.LEAVE);

            Map<Integer, String> leaveReasons = leaveRequests.stream()
                    .collect(Collectors.toMap(r -> r.getUser().getUserId(), r -> r.getReason()));
            Set<Integer> leaveRequestedUserIds = leaveReasons.keySet();

            for (org.grevo.grevobematerial.entity.Collectors c : list) {
                Users u = c.getUser();
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("id", c.getCollectorId());
                item.put("userId", u.getUserId());
                item.put("fullName", u.getFullName() != null ? u.getFullName() : "");
                item.put("email", u.getEmail() != null ? u.getEmail() : "");
                item.put("phone", u.getPhone() != null ? u.getPhone() : "");
                item.put("address", u.getAddress() != null ? u.getAddress() : "");
                item.put("vehicleType", c.getVehicleType() != null ? c.getVehicleType() : "");
                item.put("vehiclePlate", c.getVehiclePlate() != null ? c.getVehiclePlate() : "");
                item.put("maxCapacity", c.getMaxCapacity());
                item.put("currentStatus", Boolean.TRUE.equals(c.getIsOnline()) ? "ACTIVE" : "INACTIVE");
                item.put("lastActiveAt", c.getLastActiveAt());

                boolean isLeaveRequested = leaveRequestedUserIds.contains(u.getUserId());
                String reason = leaveReasons.get(u.getUserId());

                // Fallback Legacy Check
                if (!isLeaveRequested && "LEAVE_REQUESTED".equals(c.getCurrentStatus())) {
                    isLeaveRequested = true;
                    reason = c.getLeaveReason();
                }
                // Fallback Legacy Reason
                if (reason == null && c.getLeaveReason() != null) {
                    reason = c.getLeaveReason();
                }

                if (isLeaveRequested) {
                    item.put("status", "LEAVE_REQUESTED");
                    item.put("leaveReason", reason);
                } else {
                    item.put("status", "APPROVED");
                }

                if ("LEAVE_REQUESTED".equalsIgnoreCase(status) && !isLeaveRequested) {
                    continue;
                }

                result.add(item);
            }
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{collectorId}/approve-leave")
    public ResponseEntity<?> approveLeave(@PathVariable Integer collectorId) {
        Enterprise enterprise = getCurrentEnterprise();

        org.grevo.grevobematerial.entity.Collectors collector = collectorsRepository.findById(collectorId)
                .orElseThrow(() -> new RuntimeException("Collector not found"));

        if (collector.getEnterprise() == null
                || !collector.getEnterprise().getEnterpriseId().equals(enterprise.getEnterpriseId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Collector does not belong to this enterprise"));
        }

        if (!"LEAVE_REQUESTED".equals(collector.getCurrentStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Collector has not requested to leave"));
        }

        collector.setEnterprise(null);
        collector.setCurrentStatus(null);
        collector.setLeaveReason(null);
        collectorsRepository.save(collector);

        return ResponseEntity.ok(Map.of("message", "Leave request approved successfully"));
    }

    @PostMapping("/{collectorId}/reject-leave")
    public ResponseEntity<?> rejectLeave(@PathVariable Integer collectorId) {
        Enterprise enterprise = getCurrentEnterprise();

        org.grevo.grevobematerial.entity.Collectors collector = collectorsRepository.findById(collectorId)
                .orElseThrow(() -> new RuntimeException("Collector not found"));

        if (collector.getEnterprise() == null
                || !collector.getEnterprise().getEnterpriseId().equals(enterprise.getEnterpriseId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Collector does not belong to this enterprise"));
        }

        if (!"LEAVE_REQUESTED".equals(collector.getCurrentStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Collector has not requested to leave"));
        }

        // Reject leave -> Set to LEAVE_REJECTED
        collector.setCurrentStatus("LEAVE_REJECTED");
        // We keep leaveReason so collector knows which request was rejected, or just to
        // keep state until ack.
        collectorsRepository.save(collector);

        return ResponseEntity.ok(Map.of("message", "Leave request rejected. Collector notified."));
    }

    @PostMapping("/{collectorId}/approve")
    public ResponseEntity<?> approveCollector(@PathVariable Integer collectorId) {
        // collectorId is userId for Pending Requests
        Enterprise enterprise = getCurrentEnterprise();
        Users user = userRepository.findById(collectorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CollectorRequest request = collectorRequestRepository
                .findByUserAndStatusAndType(user, RequestStatus.PENDING, RequestType.JOIN)
                .or(() -> collectorRequestRepository.findByUserAndStatus(user, RequestStatus.PENDING)
                        .filter(r -> r.getType() == null))
                .orElseThrow(() -> new RuntimeException("No pending join request found for this user"));

        if (request.getType() == null) {
            request.setType(RequestType.JOIN);
        }

        if (!request.getEnterprise().getEnterpriseId().equals(enterprise.getEnterpriseId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Request does not belong to this enterprise"));
        }

        org.grevo.grevobematerial.entity.Collectors collector = collectorsRepository.findByUser(user)
                .orElseGet(() -> {
                    org.grevo.grevobematerial.entity.Collectors c = new org.grevo.grevobematerial.entity.Collectors();
                    c.setUser(user);
                    return c;
                });

        collector.setEnterprise(enterprise);
        collector.setCurrentStatus("ACTIVE");
        collectorsRepository.save(collector);

        request.setStatus(RequestStatus.APPROVED);
        collectorRequestRepository.save(request);

        return ResponseEntity.ok(Map.of("message", "Collector approved successfully"));
    }

    @PostMapping("/{collectorId}/reject")
    public ResponseEntity<?> rejectOrRemoveCollector(@PathVariable Integer collectorId) {
        Enterprise enterprise = getCurrentEnterprise();

        // 1. Try to find Active Collector by ID (for Remove action)
        Optional<org.grevo.grevobematerial.entity.Collectors> colOpt = collectorsRepository.findById(collectorId);
        if (colOpt.isPresent() && colOpt.get().getEnterprise() != null &&
                colOpt.get().getEnterprise().getEnterpriseId().equals(enterprise.getEnterpriseId())) {

            org.grevo.grevobematerial.entity.Collectors col = colOpt.get();
            col.setEnterprise(null);
            col.setCurrentStatus(null);
            col.setLeaveReason(null);
            collectorsRepository.save(col);
            return ResponseEntity.ok(Map.of("message", "Collector removed from enterprise"));
        }

        // 2. Try to find User by ID (for Reject Pending Request)
        Optional<Users> userOpt = userRepository.findById(collectorId);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            Optional<CollectorRequest> joinReq = collectorRequestRepository.findByUserAndStatusAndType(user,
                    RequestStatus.PENDING, RequestType.JOIN)
                    .or(() -> collectorRequestRepository.findByUserAndStatus(user, RequestStatus.PENDING)
                            .filter(r -> r.getType() == null));

            if (joinReq.isPresent()) {
                CollectorRequest request = joinReq.get();
                if (request.getEnterprise().getEnterpriseId().equals(enterprise.getEnterpriseId())) {
                    request.setStatus(RequestStatus.REJECTED);
                    if (request.getType() == null)
                        request.setType(RequestType.JOIN);
                    collectorRequestRepository.save(request);

                    // Clean up collector profile if accidentally set
                    collectorsRepository.findByUser(user).ifPresent(c -> {
                        if (c.getEnterprise() != null
                                && c.getEnterprise().getEnterpriseId().equals(enterprise.getEnterpriseId())) {
                            c.setEnterprise(null);
                            collectorsRepository.save(c);
                        }
                    });

                    return ResponseEntity.ok(Map.of("message", "Join request rejected"));
                }
            }
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Collector or Request not found"));
    }
}
