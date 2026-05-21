package com.example.ticketmanager.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ticketmanager.domain.Ticket;
import com.example.ticketmanager.domain.TicketStatus;
import com.example.ticketmanager.repository.TicketRepository;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class TicketDataInitializer {

	@Bean
	CommandLineRunner seedTickets(TicketRepository ticketRepository) {
		return args -> {
			if (ticketRepository.count() > 0) {
				return;
			}
			ticketRepository.saveAll(List.of(
					new Ticket("SecurityMigrationExecutor and SystemIndexMigrationExecutor should not be persistent tasks",
							"elastic/elasticsearch", "https://github.com/elastic/elasticsearch/issues/146662", TicketStatus.OPEN),
					new Ticket("Taking a snapshot of an index closed prior to 7.2.0 fails with a NPE in 7.11.2",
							"elastic/elasticsearch", "https://github.com/elastic/elasticsearch/issues/70676", TicketStatus.OPEN),
					new Ticket("ESQL should support a humanize option to quickly render values in human readable way.",
							"elastic/elasticsearch", "https://github.com/elastic/elasticsearch/issues/136533", TicketStatus.OPEN),
					new Ticket("Add search in playlists", "TeamNewPipe/NewPipe",
							"https://github.com/TeamNewPipe/NewPipe/issues/3037", TicketStatus.OPEN),
					new Ticket("See comment title \"edited\"", "TeamNewPipe/NewPipe",
							"https://github.com/TeamNewPipe/NewPipe/issues/13209", TicketStatus.OPEN),
					new Ticket("Keep history control buttons visible when scrolling up long history lists",
							"TeamNewPipe/NewPipe", "https://github.com/TeamNewPipe/NewPipe/issues/13487", TicketStatus.OPEN),
					new Ticket("[Task] Some of the sample guidance conforms to business usage", "apache/dubbo",
							"https://github.com/apache/dubbo/issues/13859", TicketStatus.OPEN),
					new Ticket("[BUG] Questions about Dubbo Adaptive Load Balance", "apache/dubbo",
							"https://github.com/apache/dubbo/issues/15810", TicketStatus.OPEN),
					new Ticket("[Task] All samples are entirely switched to SpringBoot3", "apache/dubbo",
							"https://github.com/apache/dubbo/issues/13862", TicketStatus.OPEN),
					new Ticket("kc_locale parameter not working as expected in keycloak 25 (keycloak.v3 account theme)",
							"keycloak/keycloak", "https://github.com/keycloak/keycloak/issues/36116", TicketStatus.OPEN)));
		};
	}
}
