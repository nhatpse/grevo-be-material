package org.grevo.grevobematerial.config;

import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.entity.enums.Role;
import org.grevo.grevobematerial.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                Users admin = new Users();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@grevo.org");
                admin.setRole(Role.ADMIN);
                admin.setFullName("System Admin");
                admin.setAddress("System");
                admin.setPhone("0879888965");

                userRepository.save(admin);
                System.out.println("Admin user created: username='admin', password='admin123'");
            }
        };
    }
}
