package com.example.agentic;

import com.example.agentic.orchestration.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrchestrationServiceTest {
    @Test void workflowWaitsForHumanReleaseApproval() {
        OrchestrationService service = new OrchestrationService();
        WorkflowRun run = service.start("greenfield");
        assertEquals(WorkflowRun.Status.WAITING_FOR_APPROVAL, run.status);
        assertEquals("release", run.pendingApprovalTask);
        WorkflowRun finished = service.approve(run.runId, true, "Reviewed release checklist");
        assertEquals(WorkflowRun.Status.SUCCEEDED, finished.status);
    }
    @Test void denialSafelyStopsWorkflow() {
        OrchestrationService service = new OrchestrationService();
        WorkflowRun run = service.start("ambiguous");
        WorkflowRun stopped = service.approve(run.runId, false, "Risk not accepted");
        assertEquals(WorkflowRun.Status.STOPPED, stopped.status);
    }
}
