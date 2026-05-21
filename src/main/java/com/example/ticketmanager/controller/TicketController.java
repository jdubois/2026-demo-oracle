package com.example.ticketmanager.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.ticketmanager.domain.Ticket;
import com.example.ticketmanager.repository.TicketRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	private final TicketRepository ticketRepository;

	public TicketController(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	@GetMapping
	public List<Ticket> listTickets() {
		return ticketRepository.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Ticket> getTicket(@PathVariable Long id) {
		return ticketRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Ticket> createTicket(@Valid @RequestBody TicketRequest request) {
		Ticket ticket = ticketRepository.save(toTicket(request));
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(ticket.getId())
				.toUri();
		return ResponseEntity.created(location).body(ticket);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Ticket> updateTicket(@PathVariable Long id, @Valid @RequestBody TicketRequest request) {
		return ticketRepository.findById(id)
				.map(ticket -> {
					apply(ticket, request);
					return ResponseEntity.ok(ticketRepository.save(ticket));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
		if (!ticketRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		ticketRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	private Ticket toTicket(TicketRequest request) {
		return new Ticket(request.title(), request.repository(), request.link(), request.status());
	}

	private void apply(Ticket ticket, TicketRequest request) {
		ticket.setTitle(request.title());
		ticket.setRepository(request.repository());
		ticket.setLink(request.link());
		ticket.setStatus(request.status());
	}
}
