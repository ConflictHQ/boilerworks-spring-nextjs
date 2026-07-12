# Calliope — Boilerworks Spring Boot + Next.js
<!-- Agent shim for https://github.com/calliopeai/calliope-cli -->

Primary conventions doc: [`bootstrap.md`](bootstrap.md)
Context seed: [`memory.md`](memory.md)

Read both before writing any code.

---

## Project-specific notes

- Backend is Spring Boot 3.4 (Java 24); frontend is Next.js 16 (App Router, React 19, Tailwind CSS 4). Postgres 16 + Redis 7, JPA + Flyway.
- `AuditableEntity` base class provides UUID PKs, audit fields, and soft delete — never expose integer PKs.
- Never call `repository.delete()` — use `softDelete(userId)` only.
- `@PreAuthorize` on every endpoint (group-based permissions); all responses use the `ApiResponse<T>` wrapper `{ok, data, errors}`.
- Frontend auth via the `useAuth()` hook (session cookie from Spring, httpOnly).
- Backend `./gradlew bootRun` / `./gradlew test`; frontend `npm run dev` / `npm test`.
