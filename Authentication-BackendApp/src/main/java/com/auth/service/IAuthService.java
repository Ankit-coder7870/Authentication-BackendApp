package com.auth.service;

import com.auth.dto.UserDto;

public interface IAuthService {
  
	UserDto registerUser(UserDto userDto);
}
