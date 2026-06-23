package com.auth.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
  
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
