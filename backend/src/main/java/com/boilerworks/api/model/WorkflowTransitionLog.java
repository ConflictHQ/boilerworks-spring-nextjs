package com.boilerworks.api.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "workflow_transition_logs")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowTransitionLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workflow_instance_id", nullable = false)
  private WorkflowInstance workflowInstance;

  @Column(name = "from_state", nullable = false)
  private String fromState;

  @Column(name = "to_state", nullable = false)
  private String toState;

  @Column(name = "transition_name", nullable = false)
  private String transitionName;

  @Column(name = "triggered_by")
  private UUID triggeredBy;

  @Column(name = "triggered_at", nullable = false)
  private Instant triggeredAt = Instant.now();

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata_json", columnDefinition = "TEXT")
  private Map<String, Object> metadataJson;
}
