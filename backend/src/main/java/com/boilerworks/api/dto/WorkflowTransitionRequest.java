package com.boilerworks.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class WorkflowTransitionRequest {

    @NotBlank(message = "Transition name is required")
    private String transitionName;

    private Map<String, Object> metadata;
}
