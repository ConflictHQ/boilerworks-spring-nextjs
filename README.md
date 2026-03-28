# Boilerworks Spring Boot + Next.js

> Modern enterprise Java with Spring Boot 3 backend and Next.js 16 frontend.

Spring Boot 3 paired with Next.js 16 for enterprise teams that want Java's ecosystem and type safety on the backend with a modern React frontend. Choose this over Spring + Angular when your frontend team prefers React, or when you want Server Components and the Next.js App Router.

## Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.4 (Java 24) |
| Frontend | Next.js 16 (React 19, TypeScript) |
| API | REST (session-based auth) |
| ORM | Spring Data JPA (Hibernate) |
| Database | PostgreSQL 16 |
| Cache/Sessions | Redis 7 |
| Migrations | Flyway |
| CSS | Tailwind CSS 4 |
| Tests | JUnit 5 + MockMvc (backend), Vitest (frontend) |

## Quick Start

### Docker (recommended)

```bash
cd docker && docker compose up -d --build
```

- Frontend: http://localhost:3003
- Backend API: http://localhost:8087
- Login: admin@boilerworks.dev / admin123

### Local Development

```bash
# Start infrastructure
cd docker && docker compose up -d postgres redis

# Backend (port 8087)
cd backend && ./gradlew bootRun

# Frontend (port 3003)
cd frontend && npm install && npm run dev
```

## Features

- Session-based authentication with Spring Security (httpOnly cookies, Redis-backed)
- Group-based permissions with `@PreAuthorize` on every endpoint
- Products and Categories CRUD with soft deletes and audit trails
- Forms engine (JSON Schema-based dynamic form definitions)
- Workflow engine (state machine with transition logging)
- Dark admin theme (Boilerworks branding)
- Flyway database migrations with seed data
- Docker Compose for full-stack local development
- GitHub Actions CI pipeline

## Ports

| Service | Port |
|---------|------|
| Spring Boot API | 8087 |
| Next.js UI | 3003 |
| PostgreSQL | 5446 |
| Redis | 6389 |
| Mailpit (email) | 8025 |

## Testing

```bash
# Backend
cd backend && ./gradlew test

# Frontend
cd frontend && npm test
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and the [stack primer](../primers/spring-nextjs/PRIMER.md) for architecture and conventions.
