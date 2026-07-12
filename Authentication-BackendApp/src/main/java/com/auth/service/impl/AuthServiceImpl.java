package com.auth.service.impl;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth.dto.UserDto;
import com.auth.service.IAuthService;
import com.auth.service.IUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
	

	private final IUserService userService;
	private final PasswordEncoder passwordEncoder;
	

	@Override
	public UserDto registerUser(UserDto userDto) {
		 
		userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
		UserDto userDto1 = userService.createUser(userDto);
		return userDto1;
		
	}

}
