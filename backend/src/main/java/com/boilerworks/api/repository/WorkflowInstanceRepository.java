package com.boilerworks.api.repository;

import com.boilerworks.api.model.WorkflowInstance;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {
  @Query(
      "SELECT i FROM WorkflowInstance i JOIN FETCH i.workflowDefinition"
          + " WHERE i.workflowDefinition.id = :definitionId")
  List<WorkflowInstance> findByWorkflowDefinitionId(@Param("definitionId") UUID definitionId);

  List<WorkflowInstance> findByStatus(WorkflowInstance.InstanceStatus status);
}
