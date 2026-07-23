package com.auth.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth.dto.ApiError;
import com.auth.security.JwtAuthenticationFilter;
import com.auth.security.OAuth2SuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

	private JwtAuthenticationFilter jwtAuthenticationFilter;
	private AuthenticationSuccessHandler auth2SuccessHandler;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, OAuth2SuccessHandler auth2SuccessHandler) {

		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.auth2SuccessHandler = auth2SuccessHandler;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(e -> e.disable()).cors(Customizer.withDefaults())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
						.requestMatchers("/api/v1/auth/register").permitAll().requestMatchers("/api/v1/auth/refresh")
						.permitAll().requestMatchers("/api/v1/auth/login").permitAll()
						.requestMatchers("/api/v1/auth/logout").permitAll().anyRequest().authenticated())
				.oauth2Login(oauth2 -> oauth2.successHandler(auth2SuccessHandler).failureHandler(null))
				.logout(AbstractHttpConfigurer::disable)
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, e) -> {

					e.printStackTrace();
					response.setStatus(401);
					response.setContentType("application/json");
					String message = e.getMessage();
					String error = (String) request.getAttribute("error");
					if (error != null) {
						message = error;
					}
//					Map<String, String> errorMap = Map.of("message", message, "statusCode", String.valueOf(401));
					ApiError apiError = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access", message,
							request.getRequestURI());
					ObjectMapper objectMapper = new ObjectMapper();
					response.getWriter().write(objectMapper.writeValueAsString(apiError));
				})).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

}
