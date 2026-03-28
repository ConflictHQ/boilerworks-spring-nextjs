package com.boilerworks.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormDefinitionRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Slug is required")
  private String slug;

  private String description;

  private Map<String, Object> schemaJson;

  private boolean active = true;
}
