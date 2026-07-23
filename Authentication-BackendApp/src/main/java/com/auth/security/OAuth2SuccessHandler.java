package com.auth.security;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.auth.entity.Provider;
import com.auth.entity.RefreshTokens;
import com.auth.entity.User;
import com.auth.repository.IRefreshTokenRepository;
import com.auth.repository.IUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    
	private final CookieService cookieService;
	private final IRefreshTokenRepository refreshTokenRepository;
	private final IUserRepository userRepository;
	private final JwtService jwtService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${app.auth.frontend.success-redirect}")
	private String frontEndSuccessUrl;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		logger.info("successful authenctication");
		logger.info(authentication.toString());

		OAuth2User auth2User = (OAuth2User) authentication.getPrincipal();

		// identify user
		String registrationId = "unknown";
		if (authentication instanceof OAuth2AuthenticationToken token) {
			registrationId = token.getAuthorizedClientRegistrationId();
		}

		User user = null;
		switch (registrationId) {
		case "google" -> {

			String googleId = auth2User.getAttributes().getOrDefault("sub", "").toString();
			String email = auth2User.getAttributes().getOrDefault("email", "").toString();
			String name = auth2User.getAttributes().getOrDefault("name", "").toString();
			String picture = auth2User.getAttributes().getOrDefault("picture", "").toString();

			User newUser = User.builder().email(email).enable(true).providerId(googleId).image(picture).name(name).provider(Provider.GOOGLE)
					.build();

			user = userRepository.findByEmailWithRoles(email).orElseGet(() -> userRepository.save(newUser));

		}
		
		case "github" ->{
			String gitHubId = auth2User.getAttributes().getOrDefault("id", "").toString();
			String name = auth2User.getAttributes().getOrDefault("login", "").toString();
			String image = auth2User.getAttributes().getOrDefault("avatar_url", "").toString();
			
			String email = (String)auth2User.getAttributes().get("email");
			if(email == null) {
				email = name+"@gitHub.com";
			}
			User newUser = User.builder().email(email).enable(true).providerId(gitHubId).image(image).name(name).provider(Provider.GITHUB)
					.build();
			user = userRepository.findByEmailWithRoles(email).orElseGet(() -> userRepository.save(newUser));
		}
		default -> {
			throw new RuntimeException("Invalid Registration ID");
		}
		}

		String jti = UUID.randomUUID().toString();
		RefreshTokens refreshTokenDb = RefreshTokens.builder().jti(jti).user(user).revoked(false)
				.createdAt(Instant.now()).expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
				.build();
		
		refreshTokenRepository.save(refreshTokenDb);
		
		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.refreshAccessToken(user, jti);
		
		cookieService.attachRefreshCookie(response, refreshToken, (int)jwtService.getRefreshTtlSeconds());
		

		response.sendRedirect(frontEndSuccessUrl);

	}

}
