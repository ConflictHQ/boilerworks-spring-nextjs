package com.boilerworks.api.service;

import com.boilerworks.api.dto.FormDefinitionRequest;
import com.boilerworks.api.dto.FormSubmissionRequest;
import com.boilerworks.api.model.FormDefinition;
import com.boilerworks.api.model.FormSubmission;
import com.boilerworks.api.repository.FormDefinitionRepository;
import com.boilerworks.api.repository.FormSubmissionRepository;
import com.boilerworks.api.security.BoilerworksUserDetails;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormService {

  private final FormDefinitionRepository formDefinitionRepository;
  private final FormSubmissionRepository formSubmissionRepository;

  @Transactional(readOnly = true)
  public List<FormDefinition> findAllDefinitions() {
    return formDefinitionRepository.findAll();
  }

  @Transactional(readOnly = true)
  public FormDefinition findDefinitionById(UUID id) {
    return formDefinitionRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Form definition not found: " + id));
  }

  @Transactional(readOnly = true)
  public FormDefinition findDefinitionBySlug(String slug) {
    return formDefinitionRepository
        .findBySlug(slug)
        .orElseThrow(() -> new IllegalArgumentException("Form definition not found: " + slug));
  }

  @Transactional
  public FormDefinition createDefinition(FormDefinitionRequest request) {
    if (formDefinitionRepository.existsBySlug(request.getSlug())) {
      throw new IllegalArgumentException("Form with slug already exists: " + request.getSlug());
    }

    FormDefinition form = new FormDefinition();
    form.setName(request.getName());
    form.setSlug(request.getSlug());
    form.setDescription(request.getDescription());
    form.setSchemaJson(request.getSchemaJson());
    form.setActive(request.isActive());
    return formDefinitionRepository.save(form);
  }

  @Transactional
  public FormDefinition updateDefinition(UUID id, FormDefinitionRequest request) {
    FormDefinition form = findDefinitionById(id);
    form.setName(request.getName());
    form.setSlug(request.getSlug());
    form.setDescription(request.getDescription());
    form.setSchemaJson(request.getSchemaJson());
    form.setActive(request.isActive());
    form.setVersion(form.getVersion() + 1);
    return formDefinitionRepository.save(form);
  }

  @Transactional
  public void softDeleteDefinition(UUID id, BoilerworksUserDetails currentUser) {
    FormDefinition form = findDefinitionById(id);
    form.softDelete(currentUser.getUserId());
    formDefinitionRepository.save(form);
  }

  @Transactional(readOnly = true)
  public List<FormSubmission> findSubmissions(UUID formDefinitionId) {
    return formSubmissionRepository.findByFormDefinitionId(formDefinitionId);
  }

  @Transactional
  public FormSubmission createSubmission(FormSubmissionRequest request) {
    FormDefinition form = findDefinitionById(request.getFormDefinitionId());

    FormSubmission submission = new FormSubmission();
    submission.setFormDefinition(form);
    submission.setDataJson(request.getDataJson());
    submission.setStatus(FormSubmission.SubmissionStatus.SUBMITTED);
    return formSubmissionRepository.save(submission);
  }
}
