# Claude -- Boilerworks Spring Boot + Next.js

Primary conventions doc: [`bootstrap.md`](bootstrap.md)

Read it before writing any code.

## Stack

- **Backend**: Spring Boot 3.4 (Java 24, Gradle Kotlin DSL)
- **Frontend**: Next.js 16 (App Router, React 19, Tailwind CSS 4)
- **API**: REST with session-based auth (httpOnly cookies)
- **ORM**: Spring Data JPA (Hibernate) + Flyway migrations
- **Database**: PostgreSQL 16 (port 5446)
- **Cache/Sessions**: Redis 7 (port 6389)
- **Jobs**: Spring Scheduler

## Ports

| Service   | Port |
|-----------|------|
| Spring API | 8087 |
| Next.js UI | 3003 |
| PostgreSQL | 5446 |
| Redis     | 6389 |
| Mailpit   | 8025 |

## Key Patterns

- `AuditableEntity` base class -- UUID PKs, audit fields, soft delete
- `@PreAuthorize` on every endpoint -- group-based permissions
- `ApiResponse<T>` wrapper -- `{ok, data, errors}` on all responses
- Never call `repository.delete()` -- use `softDelete(userId)` only
- Never expose integer PKs -- UUID only
- Frontend auth via `useAuth()` hook -- session cookie from Spring

## Running

```bash
# Backend
cd backend && ./gradlew bootRun

# Frontend
cd frontend && npm install && npm run dev

# Docker (full stack)
cd docker && docker compose up -d --build
```

## Testing

```bash
# Backend
cd backend && ./gradlew test

# Frontend
cd frontend && npm test
```
