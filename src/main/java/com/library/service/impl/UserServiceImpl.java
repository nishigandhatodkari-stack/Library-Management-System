package com.library.service.impl;

import com.library.entity.User;
import com.library.entity.enums.Role;
import com.library.exception.BusinessValidationException;
import com.library.repository.UserRepository;
import com.library.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User registerUser(String username, String rawPassword, String fullName, Role role) {
        String cleanUsername = username.trim().toLowerCase();
        if (userRepository.existsByUsername(cleanUsername)) {
            throw new BusinessValidationException("Username '" + cleanUsername + "' is already taken.");
        }

        User user = new User();
        user.setUsername(cleanUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName.trim());
        user.setRole(role != null ? role : Role.ROLE_LIBRARIAN);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username.trim().toLowerCase());
    }
}
