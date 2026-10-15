# Architecture Notes

```text
Angular UI :4200
  ├── POST /api/urls ──> UrlController ──> UrlService ──> H2/JPA
  ├── GET /api/urls/{code}/analytics ────────────────────┘
  └── /api/workflows ──> OrchestrationController ──> OrchestrationService
                                                    ├── Dependency graph
                                                    ├── Task/run state
                                                    ├── Approval gate
                                                    └── Audit events
```

## Workflow dependency graph
- `requirements` → `design`
- `design` → `implementation`
- `design` → `security`
- `implementation` → `tests`
- `tests` + `security` → `release` (human approval required)

The prototype's executor advances only tasks whose dependencies have succeeded. A real implementation should schedule ready independent nodes concurrently, preserve immutable input/output snapshots, and re-plan downstream work if an upstream artifact changes.

## Governance boundaries
- Release cannot complete without an explicit human decision.
- Rejection transitions the run to `STOPPED`.
- Stop endpoint provides an operator safe-stop.
- Every state transition is added to the in-memory audit trail.

## Production gaps
Use durable storage for runs/audit, authenticated approvers, tamper-evident logs, policy checks, metrics, timeouts, bounded retries, compensation/rollback, sandboxed tool execution, and real LLM agents before claiming production readiness.
