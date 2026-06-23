package com.auth.dto;

import org.springframework.http.HttpStatus;

public record ErrorResponse(String msg,HttpStatus status,int statusCode) {
      
}
