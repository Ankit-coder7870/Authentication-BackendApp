package com.auth.controller;

import java.time.Instant;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.LoginRequest;
import com.auth.dto.TokenResponse;
import com.auth.dto.UserDto;
import com.auth.entity.RefreshTokens;
import com.auth.entity.User;
import com.auth.repository.IRefreshTokenRepository;
import com.auth.repository.IUserRepository;
import com.auth.security.CookieService;
import com.auth.security.JwtService;
import com.auth.service.IAuthService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    
	private final IRefreshTokenRepository refreshTokenRepository;
	private final IUserRepository userRepository;
	private final IAuthService authService;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final ModelMapper modelMapper;
	private final CookieService cookieService;

	@PostMapping("/register")
	public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> loginUser(@RequestBody LoginRequest loginRequest,HttpServletResponse response) {

		Authentication authenicate = authenicate(loginRequest);
		User user = userRepository.findByEmail(loginRequest.email())
				.orElseThrow(() -> new BadCredentialsException("Invalid Username or Password"));
		if (!user.isEnable()) {
			throw new DisabledException("User is disabled");
		}
		
		String jti = UUID.randomUUID().toString();
		RefreshTokens refreshTokenDb = RefreshTokens.builder()
				                            .jti(jti) 
				                            .user(user)
				                            .createdAt(Instant.now())
				                            .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
				                            .revoked(false)
				                            .build();
		
		//refreshToken id and details store in DB
		refreshTokenRepository.save(refreshTokenDb);

		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.refreshAccessToken(user,refreshTokenDb.getJti());
		
		//use cookie service to attach refresh token in cookie
		cookieService.attachRefreshCookie(response, refreshToken, (int)jwtService.getRefreshTtlSeconds());
		cookieService.addNoStoreHeaders(response);
		
		TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTtlSeconds(),
				modelMapper.map(user, UserDto.class));

		return ResponseEntity.ok(tokenResponse);
	}

	public Authentication authenicate(LoginRequest loginRequest) {
		try {
			return authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
		} catch (Exception e) {
			throw new BadCredentialsException("Invalid Username or Password");
		}
	}
}
