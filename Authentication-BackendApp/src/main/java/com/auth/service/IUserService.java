package com.auth.service;

import com.auth.dto.UserDto;

public interface IUserService {

	public UserDto createUser(UserDto userdto);

	public UserDto getUserByEmail(String email);

	public UserDto updateUser(UserDto userdto, Long userId);

	public void deleteUser(Long userId);

	public UserDto getUserById(Long userId);

	Iterable<UserDto> getAllUsers();
}
