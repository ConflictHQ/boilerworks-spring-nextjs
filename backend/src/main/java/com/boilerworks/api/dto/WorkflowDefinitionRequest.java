package com.boilerworks.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowDefinitionRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Slug is required")
  private String slug;

  private String description;

  private List<Map<String, Object>> statesJson;

  private List<Map<String, Object>> transitionsJson;

  @NotBlank(message = "Initial state is required")
  private String initialState;

  private boolean active = true;
}
