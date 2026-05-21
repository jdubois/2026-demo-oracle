# OSS Ticket Radar

Ticket management application for finding good GitHub issues to help Java open source projects.

## Run locally

```bash
./mvnw spring-boot:run
```

Spring Boot starts PostgreSQL automatically through Docker Compose. Maven builds the Vue UI
automatically before the app starts, then serves it at <http://localhost:8080/>.

For hot reload:

```bash
./mvnw spring-boot:run
cd frontend
npm run dev
```

## Features

- Add, edit, remove, search, and filter GitHub tickets.
- Store tickets in PostgreSQL with Spring Data JPA.
- Seed the database with 10 Java OSS good-first-issue tickets discovered through GitHub MCP.
- Fancy Vue.js 3 UI powered by Vite, Bootstrap, and Bootstrap Icons.

## Validation

```bash
./mvnw test
./mvnw clean package
```
