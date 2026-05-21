package com.example.ticketmanager.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ticketmanager.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerTests {

	private final MockMvc mockMvc;

	@Autowired
	TicketControllerTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void shouldCreateListUpdateAndDeleteTicket() throws Exception {
		String createPayload = """
				{
				  "title": "Add a beginner-friendly Java issue",
				  "repository": "example/project",
				  "link": "https://github.com/example/project/issues/1",
				  "status": "OPEN",
				  "assigneeUsername": "julien"
				}
				""";

		String location = mockMvc.perform(post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(createPayload))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.title").value("Add a beginner-friendly Java issue"))
				.andExpect(jsonPath("$.repository").value("example/project"))
				.andExpect(jsonPath("$.status").value("OPEN"))
				.andExpect(jsonPath("$.assignee.username").value("julien"))
				.andReturn()
				.getResponse()
				.getHeader("Location");

		mockMvc.perform(get("/api/tickets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		String updatePayload = """
				{
				  "title": "Implement a beginner-friendly Java fix",
				  "repository": "example/project",
				  "link": "https://github.com/example/project/issues/1",
				  "status": "IN_PROGRESS",
				  "assigneeUsername": "bob"
				}
				""";

		mockMvc.perform(put(location)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updatePayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Implement a beginner-friendly Java fix"))
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.assignee.username").value("bob"));

		mockMvc.perform(delete(location))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(location))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldRejectInvalidTicket() throws Exception {
		String payload = """
				{
				  "title": "",
				  "repository": "not-a-repository",
				  "link": "not-a-url",
				  "status": "OPEN",
				  "assigneeUsername": "julien"
				}
				""";

		mockMvc.perform(post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldRejectUnknownAssignee() throws Exception {
		String payload = """
				{
				  "title": "Add a beginner-friendly Java issue",
				  "repository": "example/project",
				  "link": "https://github.com/example/project/issues/1",
				  "status": "OPEN",
				  "assigneeUsername": "unknown"
				}
				""";

		mockMvc.perform(post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest());
	}
}
