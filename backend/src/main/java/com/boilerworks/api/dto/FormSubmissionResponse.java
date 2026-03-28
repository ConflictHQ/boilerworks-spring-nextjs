package com.boilerworks.api.dto;

import com.boilerworks.api.model.FormSubmission;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

@Getter
public class FormSubmissionResponse {

  private final UUID id;
  private final UUID formDefinitionId;
  private final Map<String, Object> dataJson;
  private final String status;
  private final Instant createdAt;
  private final Instant updatedAt;

  public FormSubmissionResponse(FormSubmission submission) {
    this.id = submission.getId();
    this.formDefinitionId = submission.getFormDefinition().getId();
    this.dataJson = submission.getDataJson();
    this.status = submission.getStatus().name();
    this.createdAt = submission.getCreatedAt();
    this.updatedAt = submission.getUpdatedAt();
  }
}
