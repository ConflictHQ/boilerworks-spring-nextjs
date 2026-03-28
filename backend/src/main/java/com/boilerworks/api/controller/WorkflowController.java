package com.boilerworks.api.controller;

import com.boilerworks.api.dto.*;
import com.boilerworks.api.model.WorkflowDefinition;
import com.boilerworks.api.model.WorkflowInstance;
import com.boilerworks.api.security.BoilerworksUserDetails;
import com.boilerworks.api.service.WorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

  private final WorkflowService workflowService;

  @GetMapping
  @PreAuthorize("hasAuthority('workflows.view')")
  public ResponseEntity<ApiResponse<List<WorkflowDefinitionResponse>>> listDefinitions() {
    List<WorkflowDefinitionResponse> workflows =
        workflowService.findAllDefinitions().stream().map(WorkflowDefinitionResponse::new).toList();
    return ResponseEntity.ok(ApiResponse.ok(workflows));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('workflows.view')")
  public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> getDefinition(
      @PathVariable UUID id) {
    WorkflowDefinition wf = workflowService.findDefinitionById(id);
    return ResponseEntity.ok(ApiResponse.ok(new WorkflowDefinitionResponse(wf)));
  }

  @PostMapping
  @PreAuthorize("hasAuthority('workflows.create')")
  public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> createDefinition(
      @Valid @RequestBody WorkflowDefinitionRequest request) {
    try {
      WorkflowDefinition wf = workflowService.createDefinition(request);
      return ResponseEntity.ok(ApiResponse.ok(new WorkflowDefinitionResponse(wf)));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('workflows.edit')")
  public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> updateDefinition(
      @PathVariable UUID id, @Valid @RequestBody WorkflowDefinitionRequest request) {
    try {
      WorkflowDefinition wf = workflowService.updateDefinition(id, request);
      return ResponseEntity.ok(ApiResponse.ok(new WorkflowDefinitionResponse(wf)));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('workflows.delete')")
  public ResponseEntity<ApiResponse<Void>> deleteDefinition(
      @PathVariable UUID id, @AuthenticationPrincipal BoilerworksUserDetails user) {
    try {
      workflowService.softDeleteDefinition(id, user);
      return ResponseEntity.ok(ApiResponse.ok());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @PostMapping("/{id}/instances")
  @PreAuthorize("hasAuthority('workflows.execute')")
  public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> startInstance(
      @PathVariable UUID id) {
    try {
      WorkflowInstance instance = workflowService.startInstance(id);
      return ResponseEntity.ok(ApiResponse.ok(new WorkflowInstanceResponse(instance)));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @GetMapping("/{id}/instances")
  @PreAuthorize("hasAuthority('workflows.view')")
  public ResponseEntity<ApiResponse<List<WorkflowInstanceResponse>>> listInstances(
      @PathVariable UUID id) {
    List<WorkflowInstanceResponse> instances =
        workflowService.findInstancesByDefinition(id).stream()
            .map(WorkflowInstanceResponse::new)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(instances));
  }

  @PostMapping("/instances/{instanceId}/transition")
  @PreAuthorize("hasAuthority('workflows.execute')")
  public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> transition(
      @PathVariable UUID instanceId,
      @Valid @RequestBody WorkflowTransitionRequest request,
      @AuthenticationPrincipal BoilerworksUserDetails user) {
    try {
      WorkflowInstance instance = workflowService.executeTransition(instanceId, request, user);
      return ResponseEntity.ok(ApiResponse.ok(new WorkflowInstanceResponse(instance)));
    } catch (IllegalArgumentException | IllegalStateException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }
}
