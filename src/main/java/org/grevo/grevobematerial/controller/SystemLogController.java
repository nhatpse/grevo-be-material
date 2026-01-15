package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.entity.SystemLog;
import org.grevo.grevobematerial.repository.SystemLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasAuthority('ADMIN')")
public class SystemLogController {

    private final SystemLogRepository systemLogRepository;

    public SystemLogController(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    @GetMapping
    public ResponseEntity<Page<SystemLog>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(systemLogRepository
                    .findByLevelContainingIgnoreCaseOrActionContainingIgnoreCaseOrMessageContainingIgnoreCase(
                            search, search, search, pageable));
        }

        return ResponseEntity.ok(systemLogRepository.findAll(pageable));
    }
}
