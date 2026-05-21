package com.example.ticketmanager.domain;

import org.hibernate.validator.constraints.URL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false, length = 500)
	private String title;

	@NotBlank
	@Pattern(regexp = "^[\\w.-]+/[\\w.-]+$")
	@Column(nullable = false, length = 200)
	private String repository;

	@NotBlank
	@URL
	@Column(nullable = false, length = 500)
	private String link;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TicketStatus status;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "assignee_id")
	private AppUser assignee;

	protected Ticket() {
	}

	public Ticket(String title, String repository, String link, TicketStatus status, AppUser assignee) {
		this.title = title;
		this.repository = repository;
		this.link = link;
		this.status = status;
		this.assignee = assignee;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public void setStatus(TicketStatus status) {
		this.status = status;
	}

	public AppUser getAssignee() {
		return assignee;
	}

	public void setAssignee(AppUser assignee) {
		this.assignee = assignee;
	}
}
