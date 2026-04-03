package com.birbuket.authservice.controller;

import com.birbuket.authservice.dto.*;
import com.birbuket.authservice.service.AuthService;
import com.birbuket.authservice.service.RefreshTokenService;
import com.birbuket.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @Operation(summary = "User registration")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.ok().body(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> loginUser(@Valid @RequestBody UserLoginRequest request) {
        var response = authService.login(request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "User refreshToken")
    public ResponseEntity<ApiResponse<UserLoginResponse>> refresh(
            @RequestParam String refreshToken
    ) {
        var response = authService.refresh(refreshToken);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestParam String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Successfully logged out"));
    }

}
