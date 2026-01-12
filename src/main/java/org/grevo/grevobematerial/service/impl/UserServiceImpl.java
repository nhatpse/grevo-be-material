package org.grevo.grevobematerial.service.impl;

import org.grevo.grevobematerial.dto.request.ChangePasswordRequest;
import org.grevo.grevobematerial.dto.request.UpdateProfileRequest;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.repository.UserRepository;
import org.grevo.grevobematerial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Path fileStorageLocation;

    public UserServiceImpl() {
        this.fileStorageLocation = Paths.get("uploads/avatars").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

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

        // Normalize file name
        String fileName = UUID.randomUUID().toString() + "_"
                + org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Generate File Download Uri
            // Example: http://localhost:8080/uploads/avatars/abc-123.jpg
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/avatars/")
                    .path(fileName)
                    .toUriString();

            user.setAvatar(fileDownloadUri);
            return userRepository.save(user);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    @Override
    public Users removeAvatar(String username) {
        Users user = getProfile(username);
        user.setAvatar(null);
        // Optional: Delete physical file if needed
        return userRepository.save(user);
    }

    @Override
    public void deleteAccount(String username) {
        Users user = getProfile(username);
        userRepository.delete(user);
    }
}
