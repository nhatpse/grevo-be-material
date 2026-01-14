package org.grevo.grevobematerial.service.impl;

import org.grevo.grevobematerial.dto.request.LoginRequest;
import org.grevo.grevobematerial.dto.request.RegisterRequest;
import org.grevo.grevobematerial.dto.response.AuthResponse;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.repository.UserRepository;
import org.grevo.grevobematerial.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import org.grevo.grevobematerial.entity.enums.Role;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse("Username already exists!");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse("Email has already been used!");
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            return new AuthResponse("Phone number has already been used!");
        }

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setFullName(request.getFullName());
        user.setRole(Role.CITIZEN);

        userRepository.save(user);

        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getGoogleId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().name(),
                user.getAvatar());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateToken(authentication);

        Users user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getGoogleId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().name(),
                user.getAvatar());
    }

    public AuthResponse loginWithGoogle(String googleId, String email, String fullName, String avatarUrl) {
        Users user = userRepository.findByGoogleId(googleId)
                .orElse(userRepository.findByEmail(email).orElse(null));

        if (user == null) {
            user = new Users();
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setFullName(fullName);

            user.setUsername(email);
            user.setRole(Role.CITIZEN);
            user.setPassword(passwordEncoder.encode("GOOGLE_AUTH_" + UUID.randomUUID().toString()));

            userRepository.save(user);
        } else {
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }
        }

        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getGoogleId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().name(),
                user.getAvatar());
    }
}