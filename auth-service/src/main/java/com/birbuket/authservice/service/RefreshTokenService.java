package com.birbuket.authservice.service;

import com.birbuket.authservice.exception.InvalidTokenException;
import com.birbuket.authservice.models.RefreshToken;
import com.birbuket.authservice.models.UserEntity;
import com.birbuket.authservice.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(UserEntity user) {
        refreshTokenRepository.deleteByUser(user);

        var refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiration(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        var refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found!"));

        if (refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token already used!");
        }

        if (refreshToken.getExpiration().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException("Refresh token-in müddəti bitib!");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        var refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found!"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}
