package com.example.taxi.service;

import com.example.taxi.dto.response.UserResponse;
import com.example.taxi.security.AppRole;

import java.util.List;

public interface UserService {

    List<UserResponse> listAll();

    UserResponse create(String username, String rawPassword, String displayName, AppRole role);

    UserResponse updateRole(Long id, AppRole role);

    void setEnabled(Long id, boolean enabled);
}