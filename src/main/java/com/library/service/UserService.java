package com.library.service;

import com.library.entity.User;
import com.library.entity.enums.Role;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    User registerUser(String username, String rawPassword, String fullName, Role role);
    boolean existsByUsername(String username);
}
