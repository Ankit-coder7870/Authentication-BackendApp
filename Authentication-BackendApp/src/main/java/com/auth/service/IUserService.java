package com.auth.service;

import com.auth.dto.UserDto;

public interface IUserService {

	public UserDto createUser(UserDto userdto);

	public UserDto getUserByEmail(String email);

	public UserDto updateUser(UserDto userdto, String userId);

	public void deleteUser(String userId);

	public UserDto getUserById(String userId);

	Iterable<UserDto> getAllUsers();
}
