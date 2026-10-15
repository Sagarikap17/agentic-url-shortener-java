package com.example.agentic.orchestration;

import java.time.Instant;
import java.util.*;

public class WorkflowRun {
    public enum Status { PLANNED, RUNNING, WAITING_FOR_APPROVAL, SUCCEEDED, FAILED, STOPPED }
    public enum TaskStatus { BLOCKED, READY, RUNNING, SUCCEEDED, FAILED, SKIPPED }
    public static class Task {
        public String id; public String title; public List<String> dependsOn; public boolean requiresApproval; public TaskStatus status = TaskStatus.BLOCKED; public int attempts = 0; public String output;
        public Task(String id, String title, List<String> dependsOn, boolean requiresApproval) { this.id=id; this.title=title; this.dependsOn=new ArrayList<>(dependsOn); this.requiresApproval=requiresApproval; }
    }
    public final String runId = UUID.randomUUID().toString();
    public final String scenario; public final Instant createdAt = Instant.now(); public Status status = Status.PLANNED;
    public final Map<String, Task> tasks = new LinkedHashMap<>(); public final List<Map<String,Object>> audit = new ArrayList<>(); public String pendingApprovalTask;
    public WorkflowRun(String scenario) { this.scenario=scenario; }
    public void event(String type, String detail) { audit.add(Map.of("at", Instant.now().toString(), "type", type, "detail", detail)); }
}
