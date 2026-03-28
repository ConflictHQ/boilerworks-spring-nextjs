package com.boilerworks.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Slug is required")
  private String slug;

  private String description;

  private int sortOrder = 0;
}
