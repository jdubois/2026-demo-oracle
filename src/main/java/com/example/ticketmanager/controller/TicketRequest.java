package com.example.ticketmanager.controller;

import org.hibernate.validator.constraints.URL;

import com.example.ticketmanager.domain.TicketStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TicketRequest(
		@NotBlank String title,
		@NotBlank @Pattern(regexp = "^[\\w.-]+/[\\w.-]+$") String repository,
		@NotBlank @URL String link,
		@NotNull TicketStatus status,
		@NotBlank String assigneeUsername) {
}
