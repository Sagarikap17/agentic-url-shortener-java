# Agentic Software Engineering System — URL Shortener

Java 21 + Spring Boot 3 backend, Angular 19 frontend, H2 persistence, JUnit 5 tests. This is an interview assignment prototype for a URL shortener plus a governed SDLC workflow orchestrator.

## Features
- Create short URLs, optional custom aliases, HTTP/HTTPS validation, redirect and click-count analytics.
- Workflow scenarios: `greenfield`, `brownfield`, `ambiguous`.
- Explicit dependency graph: requirements → design → implementation → tests, with security review parallel to implementation/testing path after design; release waits for tests and security.
- Human approval gate before release; approval denial safely stops the workflow.
- Audit events and execution status exposed through REST APIs.
- Angular dashboard for short URL creation, analytics, workflow runs, approvals and audit trail.

## Run backend
Requirements: Java 21 and Maven 3.9+.
```bash
cd backend
mvn spring-boot:run
```
API docs are not bundled; endpoints are listed below. H2 file database is stored under `backend/data/`.

## Run frontend
Requirements: Node.js 20+ and npm.
```bash
cd frontend
npm install
npm start
```
Open http://localhost:4200. Backend runs at http://localhost:8080. CORS is configured for localhost:4200.

## Test backend
```bash
cd backend
mvn test
```

## REST API
- `POST /api/urls` body `{"url":"https://example.com/a-long-path","alias":"optional-alias"}`
- `GET /{code}` redirects and increments click count
- `GET /api/urls/{code}/analytics`
- `POST /api/workflows` body `{"scenario":"greenfield"}`
- `GET /api/workflows`
- `GET /api/workflows/{runId}`
- `POST /api/workflows/{runId}/approval` body `{"approved":true,"comment":"Reviewed"}`
- `POST /api/workflows/{runId}/stop` body `{"reason":"Risk identified"}`

## Architecture
- `url`: REST controller, service, JPA entity/repository, validation and analytics.
- `orchestration`: stateful run/task model, dependency graph, approval gate, safe-stop and audit trail.
- `frontend`: standalone Angular component with URL creation and workflow review dashboard.

## Scenario mapping
1. **Greenfield:** start a workflow to add a new URL shortener capability; inspect tasks and approve/reject release.
2. **Brownfield:** use scenario selection to represent an enhancement to an existing service; the workflow preserves the same dependency and release gates.
3. **Ambiguous:** start the ambiguous scenario and discuss how requirement normalization should ask clarifying questions before implementation. The current handler is deterministic and represents a placeholder for a real requirements agent.

## Important limitations / engineering honesty
This is a runnable prototype, not a fully autonomous coding agent or production deployment. Workflow handlers are deterministic reference handlers, not live LLM calls. Parallelizable tasks are represented in the dependency graph but this reference executor processes them sequentially. Audit and workflow state are currently in-memory; URL records persist in H2. Retry/rollback metrics, sandboxed code editing, LLM integration, authentication/authorization, rate limiting, URL abuse prevention, richer click-event records, and deployment hardening are next steps. Do not present these as already implemented.

## Recommended production hardening
- Add authentication and role-based authorization for workflow approval endpoints.
- Add rate limits, domain/URL abuse controls, expiration, and privacy-aware analytics.
- Persist workflow runs/audit events in a database and add idempotency keys.
- Add bounded retries with backoff, rollback compensation, timeouts, and metrics.
- Integrate an LLM through provider abstraction; execute generated code only in isolated containers with allowlisted tools and human approval.
