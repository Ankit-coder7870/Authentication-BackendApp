package com.auth.service.impl;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.auth.config.AppContants;
import com.auth.dto.UserDto;
import com.auth.entity.Provider;
import com.auth.entity.Role;
import com.auth.entity.User;
import com.auth.exception.ResourceNotFoundException;
import com.auth.repository.IRoleRepository;
import com.auth.repository.IUserRepository;
import com.auth.service.IUserService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements IUserService {

	private final IUserRepository userRepo;
	private final ModelMapper modelMapper;
	private final IRoleRepository roleRepo;

	@Override
	@Transactional
	public UserDto createUser(UserDto userDto) {

		if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}

		if (userRepo.existsByEmail(userDto.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}
		
		        
		User user = modelMapper.map(userDto, User.class);
		
		user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
		//role assign to user
		Role role = roleRepo.findByName(AppContants.GUEST_ROLE).orElse(null);
		System.out.println("Roles "+user.getRoles());
		 user.getRoles().add(role);

		 
		User savedUser = userRepo.save(user);
		return modelMapper.map(savedUser, UserDto.class);

	}

	@Override
	public UserDto getUserByEmail(String email) {

		User user = userRepo.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with given email id"));
		return modelMapper.map(user, UserDto.class);
	}

	@Override
	public UserDto updateUser(UserDto userDto, Long userId) {
        User existingUser = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
        if(userDto.getName() != null) existingUser.setName(userDto.getName());
        if(userDto.getImage() != null) existingUser.setImage(userDto.getImage());
        if(userDto.getProvider() != null) existingUser.setProvider(userDto.getProvider());
        if(userDto.getPassword() != null) existingUser.setPassword(userDto.getPassword());
        existingUser.setUpdatedAt(LocalDateTime.now());
        existingUser.setEnable(userDto.isEnable());
        User updateUser = userRepo.save(existingUser);
		return modelMapper.map(updateUser, UserDto.class);
	}

	@Override
	public void deleteUser(Long userId) {
		userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
		userRepo.deleteById(userId);
	}

	@Override
	public UserDto getUserById(Long userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));

		return modelMapper.map(user, UserDto.class);
	}

	@Override
	@Transactional
	public Iterable<UserDto> getAllUsers() {
		return userRepo.findAll().stream().map(user -> modelMapper.map(user, UserDto.class)).toList();
	}

}
