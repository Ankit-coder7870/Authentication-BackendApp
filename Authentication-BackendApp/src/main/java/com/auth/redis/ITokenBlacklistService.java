package com.auth.redis;

import java.time.Duration;

public interface ITokenBlacklistService {
   
	void blacklist(String jti, Duration ttl);

    boolean isBlacklisted(String jti);
}
