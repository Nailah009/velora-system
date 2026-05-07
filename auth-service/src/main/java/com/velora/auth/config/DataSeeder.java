package com.velora.auth.config;

import com.velora.auth.model.User;
import com.velora.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@velora.local");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);
            }
            if (!userRepository.existsByUsername("user")) {
                User user = new User();
                user.setUsername("user");
                user.setEmail("user@velora.local");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRole(User.Role.USER);
                userRepository.save(user);
            }
        };
    }
}
