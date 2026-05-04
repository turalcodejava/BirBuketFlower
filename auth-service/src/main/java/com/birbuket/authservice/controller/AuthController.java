package com.birbuket.authservice.controller;

import com.birbuket.authservice.dto.UserLoginRequest;
import com.birbuket.authservice.dto.UserLoginResponse;
import com.birbuket.authservice.dto.UpdateUserRequest;
import com.birbuket.authservice.dto.UpdateUserRoleRequest;
import com.birbuket.authservice.dto.ForgotPasswordRequest;
import com.birbuket.authservice.dto.ResetPasswordRequest;
import com.birbuket.authservice.dto.UserRegisterRequest;
import com.birbuket.authservice.dto.UserRegisterResponse;
import com.birbuket.authservice.service.AuthService;
import com.birbuket.authservice.service.PasswordResetService;
import com.birbuket.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @Operation(summary = "User registration")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> registerUser(
            @Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> loginUser(
            @Valid @RequestBody UserLoginRequest request) {
        var response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/users/{id}")
    @Operation(summary = "Update user profile")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.updateUser(id, request)));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.updateUserRole(id, request)));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(authService.getUserById(id)));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Şifrəni unutdum — email-ə (Kafka ilə) link göndərilir")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestForgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success(null, PasswordResetService.FORGOT_PASSWORD_GENERIC_OK));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Token ilə yeni parol təyin et")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Parol uğurla dəyişdirildi"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user from token")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return ResponseEntity.ok(ApiResponse.success(authService.getUserByUsername(username)));
    }
}