package com.boilerworks.api.controller;

import com.boilerworks.api.dto.*;
import com.boilerworks.api.model.FormDefinition;
import com.boilerworks.api.model.FormSubmission;
import com.boilerworks.api.security.BoilerworksUserDetails;
import com.boilerworks.api.service.FormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    @GetMapping
    @PreAuthorize("hasAuthority('forms.view')")
    public ResponseEntity<ApiResponse<List<FormDefinitionResponse>>> listDefinitions() {
        List<FormDefinitionResponse> forms = formService.findAllDefinitions().stream()
            .map(FormDefinitionResponse::new)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(forms));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('forms.view')")
    public ResponseEntity<ApiResponse<FormDefinitionResponse>> getDefinition(@PathVariable UUID id) {
        FormDefinition form = formService.findDefinitionById(id);
        return ResponseEntity.ok(ApiResponse.ok(new FormDefinitionResponse(form)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('forms.create')")
    public ResponseEntity<ApiResponse<FormDefinitionResponse>> createDefinition(
            @Valid @RequestBody FormDefinitionRequest request) {
        try {
            FormDefinition form = formService.createDefinition(request);
            return ResponseEntity.ok(ApiResponse.ok(new FormDefinitionResponse(form)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('forms.edit')")
    public ResponseEntity<ApiResponse<FormDefinitionResponse>> updateDefinition(
            @PathVariable UUID id, @Valid @RequestBody FormDefinitionRequest request) {
        try {
            FormDefinition form = formService.updateDefinition(id, request);
            return ResponseEntity.ok(ApiResponse.ok(new FormDefinitionResponse(form)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('forms.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteDefinition(@PathVariable UUID id,
                                                              @AuthenticationPrincipal BoilerworksUserDetails user) {
        try {
            formService.softDeleteDefinition(id, user);
            return ResponseEntity.ok(ApiResponse.ok());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasAuthority('forms.view')")
    public ResponseEntity<ApiResponse<List<FormSubmissionResponse>>> listSubmissions(@PathVariable UUID id) {
        List<FormSubmissionResponse> submissions = formService.findSubmissions(id).stream()
            .map(FormSubmissionResponse::new)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(submissions));
    }

    @PostMapping("/{id}/submissions")
    @PreAuthorize("hasAuthority('forms.submit')")
    public ResponseEntity<ApiResponse<FormSubmissionResponse>> submitForm(
            @PathVariable UUID id, @RequestBody FormSubmissionRequest request) {
        try {
            request.setFormDefinitionId(id);
            FormSubmission submission = formService.createSubmission(request);
            return ResponseEntity.ok(ApiResponse.ok(new FormSubmissionResponse(submission)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
