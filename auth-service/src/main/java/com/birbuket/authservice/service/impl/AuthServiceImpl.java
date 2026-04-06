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
import com.birbuket.authservice.mapper.UserMapper;
import com.birbuket.authservice.repository.UserRepository;
import com.birbuket.authservice.security.AuthJwtService;
import com.birbuket.authservice.service.AuthService;
import com.birbuket.common.enums.ErrorCode;
import com.birbuket.common.exception.BaseException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final AuthJwtService authJwtService;
    @Override
    @Transactional
    public UserRegisterResponse register(UserRegisterRequest request) {
        log.info("Attempting to register user: {}", request.getUsername());
        checkPasswordsMatch(request);
        checkUserExists(request);
        checkUnderage(request.getBirthDate());
        var user = userMapper.toUserEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        return userMapper.toUserRegisterResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BaseException(
                        "İstifadəçi tapılmadı",
                        HttpStatus.UNAUTHORIZED,
                        ErrorCode.UNAUTHORIZED));

        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new BaseException(
                    "Hesab aktiv deyil",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.USER_IS_NOT_ACTIVE);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseException(
                    "İstifadəçi adı və ya şifrə yanlışdır",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        }

        return UserLoginResponse.builder()
                .username(user.getUsername())
                .role(user.getRole())
                .accessToken(authJwtService.generateAccessToken(user))
                .refreshToken(authJwtService.generateRefreshToken(user))
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