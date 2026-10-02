package com.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auth.entity.Role;

@Repository
public interface IRoleRepository extends JpaRepository<Role, UUID> {
	
	Optional<Role> findByName(String name);

}
