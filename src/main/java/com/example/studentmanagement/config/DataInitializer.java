package com.example.studentmanagement.config;

import com.example.studentmanagement.entity.RoleType;
import com.example.studentmanagement.entity.User;
import com.example.studentmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initDefaultUser("admin", "admin@educaze.com", "admin123", "System Administrator", Set.of(RoleType.ROLE_ADMIN));
        initDefaultUser("lecturer", "lecturer@educaze.com", "lecturer123", "Main Lecturer", Set.of(RoleType.ROLE_LECTURER));
        initDefaultUser("student", "student@educaze.com", "student123", "Demo Student", Set.of(RoleType.ROLE_STUDENT));
    }

    private void initDefaultUser(String username, String email, String rawPassword, String fullName, Set<RoleType> roles) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User(username, email, passwordEncoder.encode(rawPassword), fullName);
            user.setRoles(roles);
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Initialized default user: '{}' with roles: {}", username, roles);
        }
    }
}
