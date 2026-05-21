package com.example.ticketmanager.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.example.ticketmanager.domain.AppUser;
import com.example.ticketmanager.repository.UserRepository;

@Configuration
public class UserDataInitializer {

	private static final List<String> DEFAULT_USERS = List.of("julien", "alice", "bob");

	@Bean
	@Order(0)
	CommandLineRunner seedUsers(UserRepository userRepository) {
		return args -> DEFAULT_USERS.stream()
				.filter(username -> userRepository.findByUsername(username).isEmpty())
				.map(AppUser::new)
				.forEach(userRepository::save);
	}
}
