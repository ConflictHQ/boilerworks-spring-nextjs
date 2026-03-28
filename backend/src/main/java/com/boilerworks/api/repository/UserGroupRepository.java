package com.boilerworks.api.repository;

import com.boilerworks.api.model.UserGroup;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
  Optional<UserGroup> findByName(String name);
}
