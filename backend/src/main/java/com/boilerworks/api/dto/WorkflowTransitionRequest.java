package com.boilerworks.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowTransitionRequest {

  @NotBlank(message = "Transition name is required")
  private String transitionName;

  private Map<String, Object> metadata;
}
