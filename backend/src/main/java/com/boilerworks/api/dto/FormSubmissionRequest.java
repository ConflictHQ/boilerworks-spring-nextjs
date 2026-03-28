package com.boilerworks.api.dto;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormSubmissionRequest {

  private UUID formDefinitionId;

  private Map<String, Object> dataJson;
}
