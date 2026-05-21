# Copilot instructions for OSS Ticket Radar

## Build, test, and run commands

Run commands from the repository root unless noted otherwise.

- Start the Spring Boot app: `./mvnw spring-boot:run`
- Start the Vue dev server: `cd frontend && npm run dev`
- Build the full app JAR, including the Vue production bundle: `./mvnw clean package`
- Run all backend tests: `./mvnw test`
- Run one test class: `./mvnw -Dtest=TicketControllerTests test`
- Run one test method: `./mvnw -Dtest=TicketControllerTests#shouldRejectInvalidTicket test`
- Build only the frontend: `npm --prefix frontend run build`
- Preview the frontend production bundle: `npm --prefix frontend run preview`

CI runs `./mvnw -B verify`, then `npm ci` and `npm run build --if-present` in `frontend/`.

## Architecture

This is a Spring Boot 4 + Vue 3 application for curating GitHub tickets that are good contribution opportunities for Java open source projects.

- The backend is under `src/main/java/com/example/ticketmanager`.
- `TicketController` exposes the REST API at `/api/tickets` and calls `TicketRepository` directly; there is no service layer for the current CRUD-only workflow.
- `Ticket` is the JPA entity. Validation constraints live on both `Ticket` and `TicketRequest`; keep repository names in `owner/repository` format and links as URLs.
- `TicketStatus` is persisted with `@Enumerated(EnumType.STRING)`. Keep API/UI status values aligned with this enum.
- `TicketDataInitializer` seeds the 10 GitHub issues when `app.seed.enabled=true` or unset, but only if the table is empty.
- Main configuration imports optional `.env` values via `spring.config.import=optional:file:.env[.properties]`; do not read or print `.env`.
- Tests use PostgreSQL Testcontainers through `TestcontainersConfiguration` and import that configuration explicitly.
- Test configuration disables seed data by default with `app.seed.enabled=false`; seed-specific tests opt back in with `@TestPropertySource`.
- The frontend is a single Vue Composition API app in `frontend/src/App.vue`, with shared styling in `frontend/src/assets/main.css`.
- Vite proxies `/api` to Spring Boot during development. Production builds go to `frontend/dist`; Maven copies that output into `target/classes/static` during `prepare-package`.

## Project-specific conventions

- Prefer Java LSP/JDTLS for Java navigation and refactoring; this repo configures it in `.github/lsp.json`.
- For Spring Boot 4 MVC tests, import `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`, not the older Spring Boot 3 package.
- Test classes that need Spring beans currently use constructor injection with `@Autowired` on the constructor.
- New backend integration tests should usually use `@SpringBootTest`, `@Import(TestcontainersConfiguration.class)`, and, for MVC endpoints, `@AutoConfigureMockMvc`.
- Keep test class names ending in `Tests` so Maven Surefire discovers them by default.
- Do not rely on seed data in general CRUD tests. Insert data through the API or repository inside the test, or explicitly enable seeding for seed-focused tests.
- Keep Vue API calls relative, e.g. `fetch('/api/tickets')`, so both Vite proxy mode and packaged Spring Boot mode work.
- Do not edit generated frontend assets in `frontend/dist` or `target/classes/static`; change `frontend/src/**` instead.
- The placeholder `src/main/resources/static/index.html` is not the packaged UI after `./mvnw clean package`; Maven overwrites the packaged static assets from `frontend/dist`.
