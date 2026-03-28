package com.boilerworks.api.repository;

import com.boilerworks.api.model.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {
    Optional<WorkflowDefinition> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<WorkflowDefinition> findByActiveTrue();
}
