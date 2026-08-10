package com.auth.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService implements ITokenBlacklistService {
	 private static final String BLACKLIST_PREFIX = "blacklist:";
	 private final RedisTemplate<String, String> redisTemplate;
	
	@Override
	public void blacklist(String jti, Duration ttl) {
		redisTemplate.opsForValue().set(
				 BLACKLIST_PREFIX + jti,
	                "true",
	                ttl
				);
		
		 System.out.println("Saved Successfully");

	}

	@Override
	public boolean isBlacklisted(String jti) {
		 Boolean exists = redisTemplate.hasKey(BLACKLIST_PREFIX + jti);

	        return Boolean.TRUE.equals(exists);
	}

}
