package com.example.ticketmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ticketmanager.domain.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

	Optional<AppUser> findByUsername(String username);
}
