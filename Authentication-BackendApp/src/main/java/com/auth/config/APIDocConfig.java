package com.auth.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Auth Application build By Ankit Anand", description = "Generic auth app that can be used by any application.", contact = @Contact(name = "Ankit Anand"), version = "1.0", summary = "This App is very useful if you don't want create auth from scratch"), security = {
		@SecurityRequirement(name = "bearerAuth") }

)
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer"
		)
public class APIDocConfig {

}
