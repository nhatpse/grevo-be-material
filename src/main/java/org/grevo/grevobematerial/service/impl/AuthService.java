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

import java.time.LocalDateTime;

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

    // Đăng ký user mới
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse("Username already exists!");
        }

        // Kiểm tra email đã tồn tại
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse("Email has already been used!");
        }

        // Kiểm tra phone đã tồn tại
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            return new AuthResponse("Phone number has already been used!");
        }

        // Tạo user mới
        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setFullName(request.getFullName());
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Tạo JWT token
        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    // Đăng nhập
    public AuthResponse login(LoginRequest request) {
        // Xác thực user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo JWT token
        String token = jwtUtils.generateToken(authentication);

        // Lấy thông tin user
        Users user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}