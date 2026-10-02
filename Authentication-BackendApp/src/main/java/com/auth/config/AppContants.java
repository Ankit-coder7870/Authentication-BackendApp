package com.auth.config;

public class AppContants {
   
	public static final String [] AUTH_PUBLIC_URl = {
			"/api/v1/auth/**",
			"/v3/api-docs/**",
			"/swagger-ui.html",
			"/swagger-ui/**",
			"/error"
	};
	
	public static final String ADMIN_ROLE = "ADMIN";
	public static final String GUEST_ROLE = "GUEST";
}
