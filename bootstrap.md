# Boilerworks Spring Boot + Next.js -- Bootstrap

> Spring Boot 3 backend with a rich Next.js 16 frontend. Modern enterprise for
> teams that want Spring's backend power with React's frontend ecosystem.

See the [Boilerworks Catalogue](../primers/CATALOGUE.md) for philosophy and universal patterns.

See the [stack primer](../primers/spring-nextjs/PRIMER.md) for stack-specific conventions and build order.

## Quick Start

### Prerequisites

- Java 24 (`/usr/bin/java`)
- Node.js 22
- PostgreSQL 16 (or Docker)
- Redis 7 (or Docker)

### Development

```bash
# Start infrastructure
cd docker && docker compose up -d postgres redis

# Backend (port 8087)
cd backend && ./gradlew bootRun

# Frontend (port 3003)
cd frontend && npm install && npm run dev
```

### Docker (full stack)

```bash
cd docker && docker compose up -d --build
```

### Default Credentials

| User | Email | Password |
|------|-------|----------|
| Admin | admin@boilerworks.dev | admin123 |
| Demo  | demo@boilerworks.dev  | demo123  |

## Architecture

```
Browser
  +-- Next.js 16 (port 3003)
        +-- REST API calls (session cookies)
              |
              v
        Spring Boot 3 (port 8087)
              |-- Spring Security (session auth, Redis-backed)
              |-- Spring Data JPA (Hibernate)
              |-- PostgreSQL 16 (port 5446)
              +-- Redis 7 (port 6389)
```

## Project Structure

```
backend/
  src/main/java/com/boilerworks/api/
    config/         -- Spring Security, JPA auditing, Redis session
    controller/     -- REST controllers (auth, products, categories, forms, workflows)
    dto/            -- Request/response DTOs
    model/          -- JPA entities (AuditableEntity base class)
    repository/     -- Spring Data JPA repositories
    security/       -- UserDetails, UserDetailsService, auth entry point
    service/        -- Business logic layer
  src/main/resources/
    db/migration/   -- Flyway SQL migrations
    application.yaml
  src/test/java/    -- JUnit 5 + MockMvc tests

frontend/
  app/
    (app)/          -- Authenticated pages (dashboard, products, categories, forms, workflows, settings)
    (login)/        -- Login page
  components/       -- UI components (sidebar, header, shadcn primitives)
  hooks/            -- Auth context, debounce
  lib/              -- API client, utilities, routes

docker/
  docker-compose.yaml
```

## Conventions

- All REST endpoints return `ApiResponse<T>` with `{ok, data, errors}`
- Every controller method has `@PreAuthorize("hasAuthority('...')")`
- Soft deletes only -- never call `.delete()` on business objects
- UUID primary keys -- never expose integer IDs
- Frontend uses dark theme by default (Boilerworks admin theme)
- Tailwind CSS 4 with shadcn-style components
