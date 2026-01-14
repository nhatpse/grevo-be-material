package org.grevo.grevobematerial.service;

import org.grevo.grevobematerial.dto.request.ChangePasswordRequest;
import org.grevo.grevobematerial.dto.request.UpdateProfileRequest;
import org.grevo.grevobematerial.entity.Users;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
        Users getProfile(String username);

        Users updateProfile(String username, UpdateProfileRequest request);

        void changePassword(String username, ChangePasswordRequest request);

        Users uploadAvatar(String username, MultipartFile file);

        Users removeAvatar(String username);

        void deleteAccount(String username);

        org.springframework.data.domain.Page<org.grevo.grevobematerial.dto.response.UserManagementResponse> getUsers(
                        String search, String role, org.springframework.data.domain.Pageable pageable);

        org.grevo.grevobematerial.dto.response.UserManagementResponse updateUser(Integer userId,
                        org.grevo.grevobematerial.dto.request.AdminUpdateUserRequest request);

        void deleteUser(Integer userId);

        void resetPassword(Integer userId);
}
