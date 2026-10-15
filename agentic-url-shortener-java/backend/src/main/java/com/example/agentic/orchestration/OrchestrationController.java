package com.example.agentic.orchestration;

import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "http://localhost:4200")
public class OrchestrationController {
    private final OrchestrationService service;
    public OrchestrationController(OrchestrationService service) { this.service=service; }
    public record StartRequest(String scenario) {}
    public record ApprovalRequest(boolean approved, String comment) {}
    @PostMapping public WorkflowRun start(@RequestBody StartRequest request) { return service.start(request.scenario()); }
    @GetMapping public Collection<WorkflowRun> list() { return service.list(); }
    @GetMapping("/{id}") public WorkflowRun get(@PathVariable String id) { return service.get(id); }
    @PostMapping("/{id}/approval") public WorkflowRun approve(@PathVariable String id, @RequestBody ApprovalRequest request) { return service.approve(id, request.approved(), request.comment()); }
    @PostMapping("/{id}/stop") public WorkflowRun stop(@PathVariable String id, @RequestBody(required=false) Map<String,String> body) { return service.stop(id, body == null ? "Manually stopped" : body.get("reason")); }
}
