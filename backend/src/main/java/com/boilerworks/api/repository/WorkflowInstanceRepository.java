package com.boilerworks.api.repository;

import com.boilerworks.api.model.WorkflowInstance;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {
  List<WorkflowInstance> findByWorkflowDefinitionId(UUID workflowDefinitionId);

  List<WorkflowInstance> findByStatus(WorkflowInstance.InstanceStatus status);
}
