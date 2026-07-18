package com.auth.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity()
@Table(name = "refresh_tokens", indexes = { @Index(name = "refresh_tokens_jti_idx", columnList = "jti", unique = true),
		@Index(name = "refresh_tokens_user_id_idx", columnList = "user_id") })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RefreshTokens {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID uuid;
    @Column(name = "jti",nullable = false,unique = true, updatable = false)
	private String jti;
    
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "user_id", nullable = false,updatable = false)
	private User user;
   
	@Column(updatable = false, nullable = false)
	private Instant createdAt;
   
	@Column(nullable = false)
	private Instant expiredAt;
	
   @Column(nullable = false)
	private boolean revoked;

	private String replacedByToken;
}
