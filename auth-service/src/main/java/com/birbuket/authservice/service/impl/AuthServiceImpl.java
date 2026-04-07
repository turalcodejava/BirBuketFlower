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
import com.birbuket.authservice.models.UserEntity;
import com.birbuket.authservice.repository.UserRepository;
import com.birbuket.authservice.service.AuthService;
import com.birbuket.authservice.service.KeycloakAdminService;
import com.birbuket.authservice.service.KeycloakTokenService;
import org.springframework.transaction.annotation.Transactional;
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
    private final KeycloakTokenService keycloakTokenService;
    private final KeycloakAdminService keycloakAdminService;
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
        UserEntity saved = userRepository.save(user);
        keycloakAdminService.provisionSkipReason().ifPresentOrElse(
                reason -> log.warn(
                        "Keycloak sinxronu ötürüldü: {}. Login üçün Keycloak-da user lazımdır (secret təyin edin və ya əl ilə user yaradın).",
                        reason),
                () -> keycloakAdminService.createUser(
                        saved.getUsername(),
                        saved.getEmail(),
                        request.getName(),
                        request.getSurname(),
                        request.getPhoneNumber(),
                        request.getPassword()));
        log.info("User registered successfully: {}", saved.getUsername());

        return userMapper.toUserRegisterResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest request) {
        var tokens = keycloakTokenService.obtainPasswordGrant(
                request.getUsername(),
                request.getPassword());

        Role role = userRepository.findByUsername(request.getUsername())
                .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))
                .map(UserEntity::getRole)
                .orElse(Role.USER);

        return UserLoginResponse.builder()
                .username(request.getUsername())
                .role(role)
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
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