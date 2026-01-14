package org.grevo.grevobematerial.controller;

import lombok.RequiredArgsConstructor;
import org.grevo.grevobematerial.dto.response.UserManagementResponse;
import org.grevo.grevobematerial.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserManagementResponse>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createAt,desc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = Sort.Direction.fromString(sortParams.length > 1 ? sortParams[1] : "asc");
        String sortBy = sortParams[0];

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<UserManagementResponse> users = userService.getUsers(search, role, pageable);

        return ResponseEntity.ok(users);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{userId}")
    public ResponseEntity<UserManagementResponse> updateUser(
            @org.springframework.web.bind.annotation.PathVariable Integer userId,
            @org.springframework.web.bind.annotation.RequestBody org.grevo.grevobematerial.dto.request.AdminUpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@org.springframework.web.bind.annotation.PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.PostMapping("/{userId}/reset-password")
    public ResponseEntity<String> resetPassword(@org.springframework.web.bind.annotation.PathVariable Integer userId) {
        userService.resetPassword(userId);
        return ResponseEntity.ok("Password reset successfully to 123456");
    }
}
