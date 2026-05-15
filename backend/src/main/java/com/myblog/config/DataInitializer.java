package com.myblog.config;

import com.myblog.entity.User;
import com.myblog.repository.UserRepository;
import com.myblog.service.AboutProfileService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AboutProfileService aboutProfileService;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AboutProfileService aboutProfileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.aboutProfileService = aboutProfileService;
    }

    @Override
    public void run(String... args) {
        initAdminUser();
        initAboutProfile();
    }

    private void initAdminUser() {
        userRepository.findByEmail("admin@myblog.com").ifPresentOrElse(
            existing -> {
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

    private void initAboutProfile() {
        aboutProfileService.ensureDefaultProfile();
    }
}
