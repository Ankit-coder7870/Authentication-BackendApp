package com.auth.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.auth.dto.UserDto;
import com.auth.entity.Provider;
import com.auth.entity.User;
import com.auth.repository.IUserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements IUserService {
	
	private final IUserRepository userRepo;
	private final ModelMapper modelMapper;

	@Override
	public UserDto createUser(UserDto userDto) {
		
		
		if(userDto.getEmail() == null || userDto.getEmail().isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		
		if(userRepo.existsByEmail(userDto.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}
		
		User user = modelMapper.map(userDto,User.class);
		user.setProvider(userDto.getProvider()!=null ? user.getProvider() : Provider.LOCAL);
		User savedUser = userRepo.save(user);
		return modelMapper.map(savedUser, UserDto.class);

	}

	@Override
	public UserDto getUserByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserDto updateUser(UserDto userdto, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteUser(String userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public UserDto getUserById(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<UserDto> getAllUsers() {
		
		return userRepo.findAll().stream().map(user -> modelMapper.map(user, UserDto.class)).toList();
	}

}
