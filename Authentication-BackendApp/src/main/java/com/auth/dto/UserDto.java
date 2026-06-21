package com.auth.dto;

import java.time.LocalDateTime;
import java.util.Set;
import com.auth.entity.Provider;
import com.auth.entity.Role;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

	private Long id;
	@Column(name = "User_name", length = 50)
	private String name;
	@Column(length = 50)
	private String email;
	private String password;
	private String image;
	private boolean enable = true;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private Provider provider;

	private Set<Role> roles;
}
