package com.boilerworks.api.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "app_users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class AppUser extends AuditableEntity {

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(name = "is_staff", nullable = false)
  private boolean staff = false;

  @Column(name = "last_login")
  private Instant lastLogin;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_group_membership",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  private Set<UserGroup> groups = new HashSet<>();

  public String getFullName() {
    return firstName + " " + lastName;
  }
}
