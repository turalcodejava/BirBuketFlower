package com.birbuket.authservice.service;

import com.birbuket.authservice.dto.UserLoginRequest;
import com.birbuket.authservice.dto.UserLoginResponse;
import com.birbuket.authservice.dto.UserRegisterRequest;
import com.birbuket.authservice.dto.UserRegisterResponse;

public interface AuthService {

    UserRegisterResponse register(UserRegisterRequest request);
    UserLoginResponse login(UserLoginRequest request);
//    UserLoginResponse refresh(String refreshToken);
}
