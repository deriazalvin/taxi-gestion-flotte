package com.example.taxi.service.impl;

import com.example.taxi.dto.response.UserResponse;
import com.example.taxi.entity.AppUser;
import com.example.taxi.exception.ConflictException;
import com.example.taxi.exception.ResourceNotFoundException;
import com.example.taxi.repository.AppUserRepository;
import com.example.taxi.security.AppRole;
import com.example.taxi.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Override
    @Transactional
    public UserResponse create(String username, String rawPassword, String displayName, AppRole role) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Le nom d'utilisateur « " + username + " » est déjà pris.");
        }
        AppUser user = new AppUser(username, passwordEncoder.encode(rawPassword), displayName, role);
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long id, AppRole role) {
        AppUser user = findEntity(id);
        user.setRole(role);
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        AppUser user = findEntity(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : id " + id));
    }
}