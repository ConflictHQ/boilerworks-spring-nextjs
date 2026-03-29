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

## API Endpoints

| Method | Path | Auth | Permission | Description |
|--------|------|------|------------|-------------|
| GET | `/health` | No | — | Health check |
| POST | `/api/auth/login` | No | — | Login with email/password |
| GET | `/api/auth/me` | Yes | — | Get current user profile |
| POST | `/api/auth/logout` | Yes | — | Logout (invalidate session) |
| GET | `/api/products` | Yes | `products.view` | List all products |
| GET | `/api/products/{id}` | Yes | `products.view` | Get product by ID |
| POST | `/api/products` | Yes | `products.create` | Create a product |
| PUT | `/api/products/{id}` | Yes | `products.edit` | Update a product |
| DELETE | `/api/products/{id}` | Yes | `products.delete` | Soft-delete a product |
| GET | `/api/categories` | Yes | `categories.view` | List all categories |
| GET | `/api/categories/{id}` | Yes | `categories.view` | Get category by ID |
| POST | `/api/categories` | Yes | `categories.create` | Create a category |
| PUT | `/api/categories/{id}` | Yes | `categories.edit` | Update a category |
| DELETE | `/api/categories/{id}` | Yes | `categories.delete` | Soft-delete a category |
| GET | `/api/forms` | Yes | `forms.view` | List form definitions |
| GET | `/api/forms/{id}` | Yes | `forms.view` | Get form definition by ID |
| POST | `/api/forms` | Yes | `forms.create` | Create a form definition |
| PUT | `/api/forms/{id}` | Yes | `forms.edit` | Update a form definition |
| DELETE | `/api/forms/{id}` | Yes | `forms.delete` | Soft-delete a form definition |
| GET | `/api/forms/{id}/submissions` | Yes | `forms.view` | List submissions for a form |
| POST | `/api/forms/{id}/submissions` | Yes | `forms.submit` | Submit a form |
| GET | `/api/workflows` | Yes | `workflows.view` | List workflow definitions |
| GET | `/api/workflows/{id}` | Yes | `workflows.view` | Get workflow definition by ID |
| POST | `/api/workflows` | Yes | `workflows.create` | Create a workflow definition |
| PUT | `/api/workflows/{id}` | Yes | `workflows.edit` | Update a workflow definition |
| DELETE | `/api/workflows/{id}` | Yes | `workflows.delete` | Soft-delete a workflow definition |
| POST | `/api/workflows/{id}/instances` | Yes | `workflows.execute` | Start a workflow instance |
| GET | `/api/workflows/{id}/instances` | Yes | `workflows.view` | List instances for a workflow |
| POST | `/api/workflows/instances/{instanceId}/transition` | Yes | `workflows.execute` | Execute a workflow transition |

## Testing

```bash
# Backend
cd backend && ./gradlew test

# Frontend
cd frontend && npm test
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and the [stack primer](../primers/spring-nextjs/PRIMER.md) for architecture and conventions.

---

Boilerworks is a [Conflict](https://weareconflict.com) brand. CONFLICT is a registered trademark of Conflict LLC.
