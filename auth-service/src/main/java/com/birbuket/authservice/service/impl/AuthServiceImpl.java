package com.birbuket.authservice.service.impl;

import com.birbuket.authservice.dto.UserLoginRequest;
import com.birbuket.authservice.dto.UserLoginResponse;
import com.birbuket.authservice.dto.UpdateUserRequest;
import com.birbuket.authservice.dto.UserRegisterRequest;
import com.birbuket.authservice.dto.UserRegisterResponse;
import com.birbuket.authservice.enums.Role;
import com.birbuket.authservice.enums.UserStatus;
import com.birbuket.authservice.exception.PasswordMismatchException;
import com.birbuket.authservice.exception.UnderageUserException;
import com.birbuket.authservice.exception.UserAlreadyExistsException;
import com.birbuket.authservice.exception.UserNotFoundException;
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
                        request.getBirthDate(),
                        request.getPassword()));
        log.info("User registered successfully: {}", saved.getUsername());

        return userMapper.toUserRegisterResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))
                .orElseThrow(() -> new UserNotFoundException("Active user not found with username " + request.getUsername()));

        var tokens = keycloakTokenService.obtainPasswordGrant(
                request.getUsername(),
                request.getPassword());

        return UserLoginResponse.builder()
                .username(request.getUsername())
                .role(user.getRole())
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .build();
    }

    @Override
    @Transactional
    public UserRegisterResponse updateUser(Long id, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));

        validateUniqueFieldsForUpdate(id, request);
        checkUnderage(request.getBirthDate());

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUsername(request.getUsername());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());

        UserEntity saved = userRepository.save(user);
        log.info("User updated successfully: id={}, username={}", saved.getId(), saved.getUsername());
        return userMapper.toUserRegisterResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRegisterResponse getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        return userMapper.toUserRegisterResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRegisterResponse getUserByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username " + username));
        return userMapper.toUserRegisterResponse(user);
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

    private void validateUniqueFieldsForUpdate(Long userId, UpdateUserRequest request) {
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userId)) {
            throw new UserAlreadyExistsException("Phone number already exists");
        }
    }

    private void checkUnderage(LocalDate date) {
        if (date == null) {
            throw new UnderageUserException("Birth date is required");
        }
        if (Period.between(date, LocalDate.now()).getYears() < 18) {
            throw new UnderageUserException("The user must be over 18 years old");
        }
    }
}