package com.birbuket.authservice.service;

import com.birbuket.authservice.dto.UserLoginRequest;
import com.birbuket.authservice.dto.UserLoginResponse;
import com.birbuket.authservice.dto.UpdateUserRequest;
import com.birbuket.authservice.dto.UpdateUserRoleRequest;
import com.birbuket.authservice.dto.UserRegisterRequest;
import com.birbuket.authservice.dto.UserRegisterResponse;

public interface AuthService {

    UserRegisterResponse register(UserRegisterRequest request);
    UserLoginResponse login(UserLoginRequest request);
    UserRegisterResponse updateUser(Long id, UpdateUserRequest request);
    UserRegisterResponse updateUserRole(Long id, UpdateUserRoleRequest request);
    UserRegisterResponse getUserById(Long id);
    UserRegisterResponse getUserByUsername(String username);
//    UserLoginResponse refresh(String refreshToken);
}
