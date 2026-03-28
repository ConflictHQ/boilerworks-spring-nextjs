package com.boilerworks.api.repository;

import com.boilerworks.api.model.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {
    List<WorkflowInstance> findByWorkflowDefinitionId(UUID workflowDefinitionId);
    List<WorkflowInstance> findByStatus(WorkflowInstance.InstanceStatus status);
}
