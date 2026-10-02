package com.auth;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.auth.config.AppContants;
import com.auth.entity.Role;
import com.auth.repository.IRoleRepository;

@SpringBootApplication
public class AuthenticationBackendAppApplication implements CommandLineRunner{
	
	@Autowired
	public IRoleRepository roleRepository;

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationBackendAppApplication.class, args);
		
	}

	@Override
	public void run(String... args) throws Exception {
		
		//GUEST
		roleRepository.findByName("ROLE_"+ AppContants.GUEST_ROLE).ifPresentOrElse(role ->{
			System.out.println("Role already exsits..."+role.getName());}, 
				() ->{
			Role role = new Role();
			role.setName("ROLE_"+AppContants.GUEST_ROLE);
			role.setId(UUID.randomUUID());
			roleRepository.save(role);
		});
		
		        //ADMIN
				roleRepository.findByName("ROLE_"+AppContants.ADMIN_ROLE).ifPresentOrElse(role ->{
					System.out.println("Role already exsits..."+role.getName());}, 
						() ->{
					Role role = new Role();
					role.setName("ROLE_"+AppContants.ADMIN_ROLE);
					role.setId(UUID.randomUUID());
					roleRepository.save(role);
				});
		
	}

}
