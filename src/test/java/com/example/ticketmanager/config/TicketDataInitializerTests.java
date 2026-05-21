package com.example.ticketmanager.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.example.ticketmanager.TestcontainersConfiguration;
import com.example.ticketmanager.repository.TicketRepository;
import com.example.ticketmanager.repository.UserRepository;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = "app.seed.enabled=true")
class TicketDataInitializerTests {

	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;

	@Autowired
	TicketDataInitializerTests(TicketRepository ticketRepository, UserRepository userRepository) {
		this.ticketRepository = ticketRepository;
		this.userRepository = userRepository;
	}

	@Test
	void shouldSeedTicketsFromPreviousGithubSearch() {
		assertThat(ticketRepository.count()).isEqualTo(10);
		assertThat(userRepository.findAll())
				.extracting("username")
				.containsExactlyInAnyOrder("julien", "alice", "bob");
		assertThat(ticketRepository.findAll())
				.anySatisfy(ticket -> {
					assertThat(ticket.getTitle()).isEqualTo("Add search in playlists");
					assertThat(ticket.getRepository()).isEqualTo("TeamNewPipe/NewPipe");
					assertThat(ticket.getLink()).isEqualTo("https://github.com/TeamNewPipe/NewPipe/issues/3037");
					assertThat(ticket.getAssignee().getUsername()).isEqualTo("julien");
				});
	}
}
