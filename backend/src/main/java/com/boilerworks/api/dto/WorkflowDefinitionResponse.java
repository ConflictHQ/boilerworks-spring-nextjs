package com.boilerworks.api.dto;

import com.boilerworks.api.model.WorkflowDefinition;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

@Getter
public class WorkflowDefinitionResponse {

  private final UUID id;
  private final String name;
  private final String slug;
  private final String description;
  private final List<Map<String, Object>> statesJson;
  private final List<Map<String, Object>> transitionsJson;
  private final String initialState;
  private final boolean active;
  private final int version;
  private final Instant createdAt;
  private final Instant updatedAt;

  public WorkflowDefinitionResponse(WorkflowDefinition wf) {
    this.id = wf.getId();
    this.name = wf.getName();
    this.slug = wf.getSlug();
    this.description = wf.getDescription();
    this.statesJson = wf.getStatesJson();
    this.transitionsJson = wf.getTransitionsJson();
    this.initialState = wf.getInitialState();
    this.active = wf.isActive();
    this.version = wf.getVersion();
    this.createdAt = wf.getCreatedAt();
    this.updatedAt = wf.getUpdatedAt();
  }
}
