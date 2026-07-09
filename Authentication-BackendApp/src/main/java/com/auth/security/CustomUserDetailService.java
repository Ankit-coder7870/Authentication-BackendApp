package com.auth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.auth.exception.ResourceNotFoundException;
import com.auth.repository.IUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
  
	private final IUserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		return userRepository.findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("Invalid Email and Password!!"));
	}

}
