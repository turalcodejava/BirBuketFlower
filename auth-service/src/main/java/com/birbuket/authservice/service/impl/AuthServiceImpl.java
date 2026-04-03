package com.birbuket.authservice.service.impl;


import com.birbuket.authservice.dto.UserLoginRequest;
import com.birbuket.authservice.dto.UserLoginResponse;
import com.birbuket.authservice.dto.UserRegisterRequest;
import com.birbuket.authservice.dto.UserRegisterResponse;
import com.birbuket.authservice.enums.Role;
import com.birbuket.authservice.enums.UserStatus;
import com.birbuket.authservice.exception.PasswordMismatchException;
import com.birbuket.authservice.exception.UnderageUserException;
import com.birbuket.authservice.exception.UserAlreadyExistsException;
import com.birbuket.authservice.exception.UserNotFoundException;
import com.birbuket.authservice.mapper.UserMapper;
import com.birbuket.authservice.models.RefreshToken;
import com.birbuket.authservice.repository.UserRepository;
import com.birbuket.authservice.service.AuthService;
import com.birbuket.authservice.service.RefreshTokenService;
import com.birbuket.common.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public UserRegisterResponse register(UserRegisterRequest request) {
        log.info("Attempting to register user: {}", request.getUsername());
        checkPasswordsMatch(request);
        checkUserExists(request);
        checkUnderage(request.getBirthDate());
        var user = userMapper.toUserEntity(request);
        user.setRole(Role.USER);
        user.setStatus(UserStatus.PENDING);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        return userMapper.toUserRegisterResponse(user);
    }

    @Override
    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getUsername());
                    return new UserNotFoundException("User not found with username: " + request.getUsername());
                });
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Password mismatch for user: {}", request.getUsername());
            throw new PasswordMismatchException("Password mismatch");
        }

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        log.info("User logged in successfully: {}", user.getUsername());
        return UserLoginResponse.builder()
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .role(Role.USER)
                .build();
    }

    @Override
    @Transactional
    public UserLoginResponse refresh(String refreshToken) {
        var verified = refreshTokenService.verifyRefreshToken(refreshToken);

        var user = verified.getUser();
        var newAccessToken = jwtService.generateToken(user);
        var newRefreshToken = refreshTokenService.createRefreshToken(user);

        return UserLoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    private void checkPasswordsMatch(UserRegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }
    }

    private void checkUserExists(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already exists");
        }
    }

    private void checkUnderage(LocalDate date) {
        if (Period.between(date, LocalDate.now()).getYears() < 18) {
            throw new UnderageUserException("The user must be over 18 years old");
        }
    }
}
