package com.auth.security;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth.redis.ITokenBlacklistService;
import com.auth.repository.IUserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final IUserRepository userRepository;
	private final ITokenBlacklistService blacklistService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String header = request.getHeader("Authorization");
		System.out.println("Header :" + header);

		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			System.out.println("Token :" + token);
			try {

				if (!jwtService.isAccessToken(token)) {
					filterChain.doFilter(request, response);
					return;
				}
				
				// checking access token is blacklisted or not
				String jti = jwtService.getJti(token);
				Boolean blacklisted = blacklistService.isBlacklisted(jti);
				System.out.println("JTI: " + jti);
				System.out.println("Blacklisted: " + blacklisted);
				if (blacklisted) {
				    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				    response.getWriter().write("Access token has been revoked");
				    return;
				}
				

				Jws<Claims> parse = jwtService.parse(token);
				Claims payload = parse.getPayload();
				long id = Long.parseLong(payload.getSubject());

				userRepository.findByIdWithRoles(id).ifPresent(user -> {
					System.out.println("User ID from token: " + id);
					if (user.isEnable()) {

						List<GrantedAuthority> authorities = user.getRoles() == null ? List.of()
								: user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName()))
										.collect(Collectors.toList());
						 System.out.println("Authorities: " + authorities);
						UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
								user.getEmail(), null, authorities);
						usernamePasswordAuthenticationToken
								.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						if (SecurityContextHolder.getContext().getAuthentication() == null)
							SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
						     System.out.println("Authentication SET");
					}
				});

			} catch (ExpiredJwtException e) {
				request.setAttribute("error", "Expired Token");
			} catch (JwtException e) {
				request.setAttribute("error", "Invalid Token");
			} catch (Exception e) {
				e.printStackTrace(); // Let unexpected errors surface
			}

		}
		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return request.getRequestURI().startsWith("/api/v1/auth")|| request.getRequestURI().equals("/error");
	}

}
