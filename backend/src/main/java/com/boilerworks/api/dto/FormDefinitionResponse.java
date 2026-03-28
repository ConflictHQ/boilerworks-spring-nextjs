package com.boilerworks.api.dto;

import com.boilerworks.api.model.FormDefinition;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

@Getter
public class FormDefinitionResponse {

  private final UUID id;
  private final String name;
  private final String slug;
  private final String description;
  private final Map<String, Object> schemaJson;
  private final boolean active;
  private final int version;
  private final Instant createdAt;
  private final Instant updatedAt;

  public FormDefinitionResponse(FormDefinition form) {
    this.id = form.getId();
    this.name = form.getName();
    this.slug = form.getSlug();
    this.description = form.getDescription();
    this.schemaJson = form.getSchemaJson();
    this.active = form.isActive();
    this.version = form.getVersion();
    this.createdAt = form.getCreatedAt();
    this.updatedAt = form.getUpdatedAt();
  }
}
