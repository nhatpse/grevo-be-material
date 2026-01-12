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

    // --- 1. ĐĂNG KÝ USER MỚI ---
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
        user.setRole(Role.CITIZEN);

        userRepository.save(user);

        // Tạo JWT token
        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name());
    }

    // --- 2. ĐĂNG NHẬP THÔNG THƯỜNG ---
    public AuthResponse login(LoginRequest request) {
        // Xác thực user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

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
                user.getFullName(),
                user.getEmail(),
                user.getRole().name());
    }

    // --- 3. ĐĂNG NHẬP BẰNG GOOGLE ---
    public AuthResponse loginWithGoogle(String googleId, String email, String fullName, String avatarUrl) {
        // Tìm user trong DB bằng googleId trước, nếu không có thì tìm theo email
        Users user = userRepository.findByGoogleId(googleId)
                .orElse(userRepository.findByEmail(email).orElse(null));

        if (user == null) {
            // Case A: User chưa tồn tại -> Tự động đăng ký
            user = new Users();
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setFullName(fullName);

            // Với Google Login, ta lấy email làm username luôn để đảm bảo unique
            user.setUsername(email);

            // Set role mặc định
            user.setRole(Role.CITIZEN);

            // Set password ngẫu nhiên (Vì họ login bằng Google nên không cần pass)
            // Phải set vì DB thường yêu cầu cột password not null
            user.setPassword(passwordEncoder.encode("GOOGLE_AUTH_" + UUID.randomUUID().toString()));

            // Nếu Entity Users của bạn có trường Avatar thì set vào
            // user.setAvatar(avatarUrl);

            userRepository.save(user);
        } else {
            // Case B: User đã tồn tại -> Cập nhật googleId nếu chưa có
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }
        }

        // Tạo JWT Token cho user này (Dùng hàm generateTokenFromUsername có sẵn)
        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name());
    }
}