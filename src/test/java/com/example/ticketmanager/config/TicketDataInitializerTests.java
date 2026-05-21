package com.example.ticketmanager.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.example.ticketmanager.TestcontainersConfiguration;
import com.example.ticketmanager.repository.TicketRepository;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = "app.seed.enabled=true")
class TicketDataInitializerTests {

	private final TicketRepository ticketRepository;

	@Autowired
	TicketDataInitializerTests(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	@Test
	void shouldSeedTicketsFromPreviousGithubSearch() {
		assertThat(ticketRepository.count()).isEqualTo(10);
		assertThat(ticketRepository.findAll())
				.anySatisfy(ticket -> {
					assertThat(ticket.getTitle()).isEqualTo("Add search in playlists");
					assertThat(ticket.getRepository()).isEqualTo("TeamNewPipe/NewPipe");
					assertThat(ticket.getLink()).isEqualTo("https://github.com/TeamNewPipe/NewPipe/issues/3037");
				});
	}
}
