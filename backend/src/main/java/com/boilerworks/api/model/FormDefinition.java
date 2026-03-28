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
@Table(name = "form_definitions")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class FormDefinition extends AuditableEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "schema_json", columnDefinition = "jsonb")
  private Map<String, Object> schemaJson;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(nullable = false)
  private int version = 1;

  @OneToMany(mappedBy = "formDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FormSubmission> submissions = new ArrayList<>();
}
