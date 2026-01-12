package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.dto.request.ChangePasswordRequest;
import org.grevo.grevobematerial.dto.request.UpdateProfileRequest;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/profile")
    public ResponseEntity<Users> getProfile() {
        return ResponseEntity.ok(userService.getProfile(getCurrentUsername()));
    }

    @PutMapping("/profile")
    public ResponseEntity<Users> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(getCurrentUsername(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(getCurrentUsername(), request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<Users> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadAvatar(getCurrentUsername(), file));
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<Users> removeAvatar() {
        return ResponseEntity.ok(userService.removeAvatar(getCurrentUsername()));
    }

    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount() {
        userService.deleteAccount(getCurrentUsername());
        return ResponseEntity.ok(Map.of("success", true, "message", "Account deleted successfully"));
    }
}
