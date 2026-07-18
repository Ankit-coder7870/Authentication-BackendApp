package com.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auth.entity.RefreshTokens;

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshTokens, UUID> {
 
	Optional<RefreshTokens> findByJti(String jti);
}
