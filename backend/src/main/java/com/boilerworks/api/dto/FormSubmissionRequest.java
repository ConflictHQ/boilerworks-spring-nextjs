package com.boilerworks.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class FormSubmissionRequest {

    private UUID formDefinitionId;

    private Map<String, Object> dataJson;
}
