package org.grevo.grevobematerial.service.impl;

import org.grevo.grevobematerial.dto.request.ChangePasswordRequest;
import org.grevo.grevobematerial.dto.request.UpdateProfileRequest;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.repository.UserRepository;
import org.grevo.grevobematerial.service.CloudinaryService;
import org.grevo.grevobematerial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public Users getProfile(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public Users updateProfile(String username, UpdateProfileRequest request) {
        Users user = getProfile(username);

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getEmail() != null)
            user.setEmail(request.getEmail());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());
        if (request.getAddress() != null)
            user.setAddress(request.getAddress());

        return userRepository.save(user);
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        Users user = getProfile(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public Users uploadAvatar(String username, MultipartFile file) {
        Users user = getProfile(username);

        try {
            String avatarUrl = cloudinaryService.uploadImage(file, "grevo/avatars");
            user.setAvatar(avatarUrl);
            return userRepository.save(user);

        } catch (IOException ex) {
            throw new RuntimeException("Could not upload avatar. Please try again!", ex);
        }
    }

    @Override
    public Users removeAvatar(String username) {
        Users user = getProfile(username);

        user.setAvatar(null);
        return userRepository.save(user);
    }

    @Override
    public void deleteAccount(String username) {
        Users user = getProfile(username);
        userRepository.delete(user);
    }

    @Override
    public org.springframework.data.domain.Page<org.grevo.grevobematerial.dto.response.UserManagementResponse> getUsers(
            String search, String role, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Users> spec = (root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String likeSearch = "%" + search.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likeSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likeSearch)));
            }

            if (role != null && !role.trim().isEmpty() && !role.equalsIgnoreCase("ALL")) {
                try {
                    org.grevo.grevobematerial.entity.enums.Role roleEnum = org.grevo.grevobematerial.entity.enums.Role
                            .valueOf(role.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("role"), roleEnum));
                } catch (IllegalArgumentException e) {
                }
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Page<Users> usersPage = userRepository.findAll(spec, pageable);

        return usersPage.map(user -> org.grevo.grevobematerial.dto.response.UserManagementResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .address(user.getAddress())
                .createdAt(user.getCreateAt())
                .updatedAt(user.getUpdateAt())
                .build());
    }

    @Override
    public org.grevo.grevobematerial.dto.response.UserManagementResponse updateUser(Integer userId,
            org.grevo.grevobematerial.dto.request.AdminUpdateUserRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        user = userRepository.save(user);

        return org.grevo.grevobematerial.dto.response.UserManagementResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .address(user.getAddress())
                .createdAt(user.getCreateAt())
                .updatedAt(user.getUpdateAt())
                .build();
    }

    @Override
    public void deleteUser(Integer userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public void resetPassword(Integer userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
    }
}
