package com.vitalink.platform.config;

import com.vitalink.platform.entity.Role;
import com.vitalink.platform.entity.User;
import com.vitalink.platform.entity.enums.RoleName;
import com.vitalink.platform.entity.enums.UserStatus;
import com.vitalink.platform.repository.RoleRepository;
import com.vitalink.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            log.info("Usuario administrador ja existe ({}). Seed ignorado.", adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "Perfil " + RoleName.ADMIN + " ausente. Verifique a migration V2__seed_roles.sql"));

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .fullName("Administrador do Sistema")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Usuario administrador inicial criado: {}", adminEmail);
    }
}
