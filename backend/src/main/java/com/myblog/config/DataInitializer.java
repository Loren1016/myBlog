package com.myblog.config;

import com.myblog.entity.User;
import com.myblog.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        userRepository.findByEmail("admin@myblog.com").ifPresentOrElse(
            existing -> {
                // 如果密码哈希损坏（非 bcrypt 格式），自动修复
                if (!existing.getPasswordHash().startsWith("$2a$")
                        && !existing.getPasswordHash().startsWith("$2b$")
                        && !existing.getPasswordHash().startsWith("$2y$")) {
                    existing.setPasswordHash(passwordEncoder.encode("admin123"));
                    userRepository.save(existing);
                }
            },
            () -> {
                User admin = new User();
                admin.setEmail("admin@myblog.com");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setDisplayName("管理员");
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }
        );
    }
}
