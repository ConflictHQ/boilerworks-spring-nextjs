package com.boilerworks.api.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "workflow_instances")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowInstance extends AuditableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workflow_definition_id", nullable = false)
  private WorkflowDefinition workflowDefinition;

  @Column(name = "current_state", nullable = false)
  private String currentState;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "context_json", columnDefinition = "TEXT")
  private Map<String, Object> contextJson;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private InstanceStatus status = InstanceStatus.ACTIVE;

  @OneToMany(mappedBy = "workflowInstance", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("triggeredAt ASC")
  private List<WorkflowTransitionLog> transitionLogs = new ArrayList<>();

  public enum InstanceStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED,
    ERROR
  }
}
