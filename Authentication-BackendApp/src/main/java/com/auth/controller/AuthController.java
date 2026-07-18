package com.auth.controller;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import com.auth.dto.RefreshTokenRequest;
import com.auth.dto.TokenResponse;
import com.auth.dto.UserDto;
import com.auth.entity.RefreshTokens;
import com.auth.entity.User;
import com.auth.repository.IRefreshTokenRepository;
import com.auth.repository.IUserRepository;
import com.auth.security.CookieService;
import com.auth.security.JwtService;
import com.auth.service.IAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.Cookie;

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

	// refresh token and access token renew krne k liye
	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false) RefreshTokenRequest body,
			HttpServletResponse response, HttpServletRequest request) {

		String refreshToken = readRefreshToken(body, request)
				.orElseThrow(() -> new BadCredentialsException("Refresh Token is missing"));

		if (!jwtService.isRefreshToken(refreshToken)) {
			throw new BadCredentialsException("Invalid Refresh Token Type");
		}

		String jti = jwtService.getJti(refreshToken);
		Long userId = jwtService.getUserId(refreshToken);
		RefreshTokens storedRefreshToken = refreshTokenRepository.findByJti(jti)
				.orElseThrow(() -> new BadCredentialsException("Refresh Token not recognized"));

		if (storedRefreshToken.isRevoked()) {
			throw new BadCredentialsException("Refresh Token expired or revoked");
		}

		if (storedRefreshToken.getExpiredAt().isBefore(Instant.now())) {
			throw new BadCredentialsException("Refresh Token expired ");
		}

		if (!storedRefreshToken.getUser().getId().equals(userId)) {
			throw new BadCredentialsException("Refresh Token does not belong to this user");
		}

		// refresh token ko rotate
		storedRefreshToken.setRevoked(true);
		String newJti = UUID.randomUUID().toString();
		storedRefreshToken.setReplacedByToken(newJti);
		refreshTokenRepository.save(storedRefreshToken);

		User user = storedRefreshToken.getUser();

		var newRefreshTokenDb = RefreshTokens.builder().jti(newJti).user(user).createdAt(Instant.now())
				.expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())).build();
         refreshTokenRepository.save(newRefreshTokenDb);
         
         String newRefreshToken = jwtService.refreshAccessToken(user, newJti);
         String newAccessToken = jwtService.generateAccessToken(user);
         
         cookieService.attachRefreshCookie(response, newRefreshToken,(int)jwtService.getRefreshTtlSeconds());
         cookieService.addNoStoreHeaders(response);
          
         return ResponseEntity.ok(TokenResponse.of(newAccessToken, newRefreshToken, jwtService.getAccessTtlSeconds(), modelMapper.map(user, UserDto.class)));

	}

	// 1.this method will read refresh token from request header or body
	private Optional<String> readRefreshToken(RefreshTokenRequest body, HttpServletRequest request) {

		// prefer reading refresh token from cookie
		if (request.getCookies() != null) {
			Optional<String> fromCookie = Arrays.stream(request.getCookies())
					.filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName())).map(Cookie::getValue)
					.filter(v -> !v.isBlank()).findFirst();

			if (fromCookie.isPresent()) {
				return fromCookie;
			}
		}

		// 2.reading from requestBody
		if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
			return Optional.of(body.refreshToken());
		}

		// 3. Custom header
		String refreshHeader = request.getHeader("X-Refresh-Token");
		if (refreshHeader != null && !refreshHeader.isBlank()) {
			return Optional.of(refreshHeader.trim());
		}

		return Optional.empty();
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> loginUser(@RequestBody LoginRequest loginRequest,
			HttpServletResponse response) {

		Authentication authenicate = authenicate(loginRequest);
		User user = userRepository.findByEmail(loginRequest.email())
				.orElseThrow(() -> new BadCredentialsException("Invalid Username or Password"));
		if (!user.isEnable()) {
			throw new DisabledException("User is disabled");
		}

		String jti = UUID.randomUUID().toString();
		RefreshTokens refreshTokenDb = RefreshTokens.builder().jti(jti).user(user).createdAt(Instant.now())
				.expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())).revoked(false).build();

		// refreshToken id and details store in DB
		refreshTokenRepository.save(refreshTokenDb);

		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.refreshAccessToken(user, refreshTokenDb.getJti());

		// use cookie service to attach refresh token in cookie
		cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
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
