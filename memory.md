# Boilerworks Memory

This file is the **AI context seed** for the Boilerworks Spring Boot + Next.js
template. It captures decisions, constraints, and non-obvious facts that are
not derivable from reading the code.

For conventions and patterns, see [`bootstrap.md`](bootstrap.md).

---

## Template purpose

Catalogue template: Spring Boot 3 REST backend + Next.js 16 frontend, for
enterprise teams that want Java on the backend with a React frontend. New
projects are seeded from this repo -- keep it minimal, conventional, and green.

---

## Key architectural decisions

| Decision | Why |
|---|---|
| Session auth, not JWT | Spring Security issues an httpOnly session cookie; sessions live in Redis (Spring Session). The frontend never handles tokens -- `useAuth()` reads `/api/auth/me` |
| `ApiResponse<T>` wrapper | Every REST endpoint returns `{ok, data, errors}` so the frontend has one error-handling path |
| `AuditableEntity` base class | UUID PKs, created/updated audit fields, soft delete -- all business entities extend it |
| `@PreAuthorize` on every endpoint | Group-based permissions (`items.view`, `forms.submit`, ...); no unguarded controller methods |
| Flyway owns the schema | `V1__initial_schema.sql` + `V2__seed_data.sql`; Hibernate never auto-migrates. Seed users: admin@boilerworks.dev / demo@boilerworks.dev |
| Spotless enforces Java formatting | CI runs `spotlessCheck`; run `./gradlew spotlessApply` before committing backend changes |

---

## Port scheme (non-obvious)

The application defaults are deliberately non-standard so multiple Boilerworks
stacks can run side by side on one machine: API 8087, UI 3003, PostgreSQL 5446,
Redis 6389. The Docker full stack (`docker/docker-compose.yaml`) maps them back
to conventional host ports (8000 / 3000 / 5432 / 6379). CI maps 5446:5432 and
6389:6379 for the service containers.

---

## Things that bite newcomers

- **Never call `repository.delete()`** -- use `softDelete(userId)` only.
- **Never expose integer PKs** -- UUID only.
- **CORS origins are hardcoded** in `SecurityConfig.corsConfigurationSource()`
  (`http://localhost:3003`, `http://127.0.0.1:3003`) -- there is no
  `CORS_ORIGINS` environment variable.
- **Two API paths from the frontend**: `next.config.ts` rewrites `/api/:path*`
  to the backend (server-side proxy), but `frontend/lib/api.ts` calls the
  backend directly via `NEXT_PUBLIC_API_ROOT` (default `http://localhost:8087`).
  Changing one without the other breaks either SSR or the browser.
- **Frontend tests are Vitest, backend JUnit 5 + MockMvc** -- `npm test` and
  `./gradlew test` respectively; both run in CI.
