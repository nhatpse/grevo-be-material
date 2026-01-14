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
}
