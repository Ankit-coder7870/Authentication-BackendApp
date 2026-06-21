package com.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auth.entity.User;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
  
	public boolean existsByEmail(String email);
}
