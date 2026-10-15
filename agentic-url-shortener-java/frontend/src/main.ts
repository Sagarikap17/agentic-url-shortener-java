import { bootstrapApplication } from '@angular/platform-browser';
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, provideHttpClient } from '@angular/common/http';

interface WorkflowTask {
  id: string;
  title: string;
  status: string;
  attempts: number;
}

interface WorkflowResponse {
  runId: string;
  status: string;
  tasks: Record<string, WorkflowTask>;
  audit: unknown[];
}

@Component({ selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule], template: `
<main class="shell"><header><div><p class="eyebrow">AGENTIC ENGINEERING LAB</p><h1>Shorten. Orchestrate. Verify.</h1><p class="sub">URL shortener + governed SDLC workflow prototype</p></div><span class="pill">Human-in-the-loop</span></header>
<section class="panel"><h2>Create a short URL</h2><form (ngSubmit)="shorten()"><label>Destination URL<input name="url" [(ngModel)]="url" required type="url" placeholder="https://example.com/long/path"></label><label>Custom alias <span class="muted">(optional)</span><input name="alias" [(ngModel)]="alias" placeholder="my-link"></label><button [disabled]="busy">{{busy ? 'Creating…' : 'Create short URL'}}</button></form><p class="error" *ngIf="error">{{error}}</p><div class="result" *ngIf="created"><strong>Created!</strong><a [href]="created.shortUrl" target="_blank">{{created.shortUrl}}</a><button class="secondary" (click)="loadAnalytics(created.code)">View analytics</button></div><div class="result" *ngIf="analytics"><strong>Analytics</strong><span>{{analytics.clicks}} clicks</span><small>{{analytics.originalUrl}}</small></div></section>
<section class="panel"><div class="section-head"><div><h2>Agentic workflow</h2><p class="muted">Run a scenario through dependency gates and human release approval.</p></div></div><div class="actions"><select name="scenario" [(ngModel)]="scenario"><option value="greenfield">Greenfield: new feature</option><option value="brownfield">Brownfield: existing system change</option><option value="ambiguous">Ambiguous: clarify requirements</option></select><button (click)="startWorkflow()" [disabled]="busy">Start workflow</button></div><p class="error" *ngIf="workflowError">{{workflowError}}</p><div *ngIf="workflow" class="workflow"><div class="status"><span>Run {{workflow.runId | slice:0:8}}</span><strong>{{workflow.status}}</strong></div><div class="task" *ngFor="let task of workflow.tasks | keyvalue"><div><strong>{{task.value.title}}</strong><small>{{task.value.id}} · attempts: {{task.value.attempts}}</small></div><span class="task-status">{{task.value.status}}</span></div><div class="approval" *ngIf="workflow.status === 'WAITING_FOR_APPROVAL'"><h3>Release approval required</h3><p>Review the task graph and validation status before approving.</p><input [(ngModel)]="comment" name="comment" placeholder="Approval/rejection rationale"><div class="actions"><button (click)="approve(true)">Approve release</button><button class="danger" (click)="approve(false)">Reject & safe-stop</button></div></div><details><summary>Audit trail</summary><pre>{{workflow.audit | json}}</pre></details></div></section><footer>Prototype note: workflow handlers are deterministic reference agents. Plug in an LLM provider and sandboxed tools for true AI-driven code changes.</footer></main>` })
export class AppComponent {
  private http = inject(HttpClient); url = ''; alias = ''; scenario = 'greenfield'; comment = ''; busy = false; error = ''; workflowError = ''; created: any = null; analytics: any = null; workflow: WorkflowResponse | null = null;
  shorten() { this.busy=true; this.error=''; this.http.post<any>('http://localhost:8080/api/urls',{url:this.url,alias:this.alias || null}).subscribe({next:r=>{this.created=r;this.analytics=null;this.busy=false;},error:e=>{this.error=e.error?.error || 'Could not create URL. Is the backend running?';this.busy=false;}}); }
  loadAnalytics(code:string) { this.http.get<any>(`http://localhost:8080/api/urls/${encodeURIComponent(code)}/analytics`).subscribe({next:r=>this.analytics=r,error:()=>this.error='Could not load analytics.'}); }
  startWorkflow() { this.workflowError=''; this.http.post<WorkflowResponse>('http://localhost:8080/api/workflows',{scenario:this.scenario}).subscribe({next:r=>this.workflow=r,error:e=>this.workflowError=e.error?.message || 'Could not start workflow.'}); }
  approve(approved:boolean) { if(!this.workflow)return; this.http.post<WorkflowResponse>(`http://localhost:8080/api/workflows/${this.workflow.runId}/approval`,{approved,comment:this.comment}).subscribe({next:r=>this.workflow=r,error:e=>this.workflowError=e.error?.message || 'Approval action failed.'}); }
}
bootstrapApplication(AppComponent,{providers:[provideHttpClient()]}).catch(err=>console.error(err));
