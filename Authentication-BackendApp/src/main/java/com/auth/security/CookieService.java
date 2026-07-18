package com.auth.security;

import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseCookie.ResponseCookieBuilder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Service
@Getter
public class CookieService {

	private final String refreshTokenCookieName;
	private final boolean cookieHttpOnly;
	private final boolean cookieSecure;
	private final String cookieDomain;
	private final String cookieSameSite;

	public CookieService(@Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
			@Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
			@Value("${security.jwt.cookie-secure}") boolean cookieSecure,
			@Value("${security.jwt.cookie-domain}") String cookieDomain,
			@Value("${security.jwt.cookie-same-site}") String cookieSameSite) {

		this.refreshTokenCookieName = refreshTokenCookieName;
		this.cookieHttpOnly = cookieHttpOnly;
		this.cookieSecure = cookieSecure;
		this.cookieDomain = cookieDomain;
		this.cookieSameSite = cookieSameSite;
	}

	// create a method to attach cookie to response
	public void attachRefreshCookie(HttpServletResponse response, String value, int maxAge) {
		ResponseCookieBuilder reponseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, value)
				.httpOnly(cookieHttpOnly).secure(cookieSecure).path("/").maxAge(maxAge).sameSite(cookieSameSite);

		if (cookieDomain != null && !cookieDomain.isBlank()) {
			reponseCookieBuilder.domain(cookieDomain);
		}

		ResponseCookie responseCookie = reponseCookieBuilder.build();
		response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
	}

	public void clearRefreshToken(HttpServletResponse response) {
		ResponseCookieBuilder reponseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, "")
				.httpOnly(cookieHttpOnly).secure(cookieSecure).maxAge(0).path("/").sameSite(cookieSameSite);

		if (cookieDomain != null && !cookieDomain.isBlank()) {
			reponseCookieBuilder.domain(cookieDomain);
		}

		ResponseCookie responseCookie = reponseCookieBuilder.build();
		response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
	}
	
	public void addNoStoreHeaders(HttpServletResponse response) {
		response.addHeader(HttpHeaders.CACHE_CONTROL, "no-store");
		response.addHeader("Pragma", "no-cache");
	}

}
