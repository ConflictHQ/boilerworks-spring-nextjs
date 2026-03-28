package com.boilerworks.api.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_groups")
@Getter
@Setter
@NoArgsConstructor
public class UserGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column private String description;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "group_permissions",
      joinColumns = @JoinColumn(name = "group_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions = new HashSet<>();

  @ManyToMany(mappedBy = "groups")
  private Set<AppUser> users = new HashSet<>();

  public UserGroup(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
