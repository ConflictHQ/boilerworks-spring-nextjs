package com.boilerworks.api.repository;

import com.boilerworks.api.model.FormSubmission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, UUID> {
  List<FormSubmission> findByFormDefinitionId(UUID formDefinitionId);
}
