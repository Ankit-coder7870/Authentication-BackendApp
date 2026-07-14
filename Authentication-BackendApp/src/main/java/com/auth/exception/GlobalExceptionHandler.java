package com.auth.exception;




import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth.dto.ApiError;
import com.auth.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler({
		BadCredentialsException.class,
		CredentialsExpiredException.class,
		UsernameNotFoundException.class,
		DisabledException.class
	})
	public ResponseEntity<ApiError> handleAuthException(Exception e,HttpServletRequest request){
	    ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", e.getMessage(), request.getRequestURI());
	    return ResponseEntity.badRequest().body(apiError);
	}
  
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception){
		 ErrorResponse internalServerError = new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND, 404);
		 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(internalServerError);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception){
		 ErrorResponse internalServerError = new ErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST, 400);
		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(internalServerError);
	}
}
