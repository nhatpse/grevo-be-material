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

            // Create 10 Citizens
            for (int i = 1; i <= 10; i++) {
                String username = "citizen" + i;
                if (!userRepository.existsByUsername(username)) {
                    Users user = new Users();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setEmail(username + "@grevo.org");
                    user.setRole(Role.CITIZEN);
                    user.setFullName("Citizen " + i);
                    user.setAddress("Hanoi, Vietnam");
                    user.setPhone("09000000" + (i < 10 ? "0" + i : i));
                    user.setIsActive(true);
                    userRepository.save(user);
                }
            }
            System.out.println("10 Citizens created");

            // Create 20 Collectors
            for (int i = 1; i <= 20; i++) {
                String username = "collector" + i;
                if (!userRepository.existsByUsername(username)) {
                    Users user = new Users();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setEmail(username + "@grevo.org");
                    user.setRole(Role.COLLECTOR);
                    user.setFullName("Collector " + i);
                    user.setAddress("Hanoi, Vietnam");
                    user.setPhone("09100000" + (i < 10 ? "0" + i : i));
                    user.setIsActive(true);
                    userRepository.save(user);
                }
            }
            System.out.println("20 Collectors created");

            // Create 5 Enterprises
            for (int i = 1; i <= 5; i++) {
                String username = "enterprise" + i;
                if (!userRepository.existsByUsername(username)) {
                    Users user = new Users();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setEmail(username + "@grevo.org");
                    user.setRole(Role.ENTERPRISE);
                    user.setFullName("Enterprise " + i);
                    user.setAddress("Hanoi, Vietnam");
                    user.setPhone("09200000" + (i < 10 ? "0" + i : i));
                    user.setIsActive(true);
                    userRepository.save(user);
                }
            }
            System.out.println("5 Enterprises created");
        };
    }
}
