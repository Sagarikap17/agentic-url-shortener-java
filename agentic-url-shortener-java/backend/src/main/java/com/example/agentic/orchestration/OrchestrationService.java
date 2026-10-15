package com.example.agentic.orchestration;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrchestrationService {
    private final Map<String, WorkflowRun> runs = new ConcurrentHashMap<>();
    public WorkflowRun start(String scenario) {
        if (!Set.of("greenfield", "brownfield", "ambiguous").contains(scenario)) throw new IllegalArgumentException("scenario must be greenfield, brownfield, or ambiguous");
        WorkflowRun run = new WorkflowRun(scenario);
        run.tasks.put("requirements", new WorkflowRun.Task("requirements", "Normalize requirements and assumptions", List.of(), false));
        run.tasks.put("design", new WorkflowRun.Task("design", "Produce architecture and API design", List.of("requirements"), false));
        run.tasks.put("implementation", new WorkflowRun.Task("implementation", "Implement backend/frontend changes", List.of("design"), false));
        run.tasks.put("tests", new WorkflowRun.Task("tests", "Run unit and integration validation", List.of("implementation"), false));
        run.tasks.put("security", new WorkflowRun.Task("security", "Review security, policy and failure modes", List.of("design"), false));
        run.tasks.put("release", new WorkflowRun.Task("release", "Approve release readiness", List.of("tests", "security"), true));
        run.event("RUN_CREATED", "Scenario=" + scenario); runs.put(run.runId, run); advance(run); return run;
    }
    public Collection<WorkflowRun> list() { return runs.values(); }
    public WorkflowRun get(String id) { WorkflowRun run=runs.get(id); if(run==null) throw new NoSuchElementException("Workflow run not found"); return run; }
    public WorkflowRun approve(String id, boolean approved, String comment) {
        WorkflowRun run=get(id);
        if (run.status != WorkflowRun.Status.WAITING_FOR_APPROVAL || run.pendingApprovalTask == null) throw new IllegalStateException("Run is not waiting for approval");
        WorkflowRun.Task task=run.tasks.get(run.pendingApprovalTask); run.event(approved?"APPROVAL_GRANTED":"APPROVAL_DENIED", comment == null ? "" : comment);
        run.pendingApprovalTask=null;
        if (approved) { task.status=WorkflowRun.TaskStatus.SUCCEEDED; task.output="Human approval granted"; advance(run); }
        else { task.status=WorkflowRun.TaskStatus.SKIPPED; run.status=WorkflowRun.Status.STOPPED; run.event("SAFE_STOP", "Release approval denied"); }
        return run;
    }
    public WorkflowRun stop(String id, String reason) { WorkflowRun run=get(id); run.status=WorkflowRun.Status.STOPPED; run.event("SAFE_STOP", reason == null ? "Manually stopped" : reason); return run; }
    private void advance(WorkflowRun run) {
        run.status=WorkflowRun.Status.RUNNING;
        boolean progressed;
        do {
            progressed=false;
            for (WorkflowRun.Task task:run.tasks.values()) {
                if(task.status != WorkflowRun.TaskStatus.BLOCKED && task.status != WorkflowRun.TaskStatus.READY) continue;
                boolean depsDone=task.dependsOn.stream().allMatch(dep -> run.tasks.get(dep).status==WorkflowRun.TaskStatus.SUCCEEDED);
                if(!depsDone) continue;
                task.status=WorkflowRun.TaskStatus.READY;
                if(task.requiresApproval) { run.status=WorkflowRun.Status.WAITING_FOR_APPROVAL; run.pendingApprovalTask=task.id; run.event("APPROVAL_REQUIRED", task.title); return; }
                task.status=WorkflowRun.TaskStatus.RUNNING; task.attempts++;
                // Deterministic reference handlers; replace with bounded LLM/tool execution in a production implementation.
                task.output="Reference handler completed for scenario: " + run.scenario;
                task.status=WorkflowRun.TaskStatus.SUCCEEDED; run.event("TASK_SUCCEEDED", task.id); progressed=true;
            }
        } while(progressed);
        if(run.tasks.values().stream().allMatch(t -> t.status==WorkflowRun.TaskStatus.SUCCEEDED)) { run.status=WorkflowRun.Status.SUCCEEDED; run.event("RUN_SUCCEEDED", "All tasks completed"); }
    }
}
