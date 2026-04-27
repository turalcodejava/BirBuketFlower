package com.birbuket.authservice.repository;

import com.birbuket.authservice.models.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByTokenHashAndUsedIsFalse(String tokenHash);

    @Modifying
    @Query("update PasswordResetTokenEntity p set p.used = true where p.user.id = :userId and p.used = false")
    int markUnusedTokensUsedForUser(@Param("userId") Long userId);
}
