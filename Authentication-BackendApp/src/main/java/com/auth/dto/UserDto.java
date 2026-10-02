package com.auth.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
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
	@Column( length = 50)
	private String name;
	@Column(length = 50)
	private String email;
	private String password;
	private String image;
	private boolean enable = true;

	private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

	private Provider provider = Provider.LOCAL;
    private Set<RoleDto> roles = new HashSet<>();
}
