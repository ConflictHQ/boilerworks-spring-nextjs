package com.boilerworks.api.dto;

import com.boilerworks.api.model.WorkflowInstance;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
public class WorkflowInstanceResponse {

    private final UUID id;
    private final UUID workflowDefinitionId;
    private final String workflowName;
    private final String currentState;
    private final Map<String, Object> contextJson;
    private final String status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public WorkflowInstanceResponse(WorkflowInstance instance) {
        this.id = instance.getId();
        this.workflowDefinitionId = instance.getWorkflowDefinition().getId();
        this.workflowName = instance.getWorkflowDefinition().getName();
        this.currentState = instance.getCurrentState();
        this.contextJson = instance.getContextJson();
        this.status = instance.getStatus().name();
        this.createdAt = instance.getCreatedAt();
        this.updatedAt = instance.getUpdatedAt();
    }
}
