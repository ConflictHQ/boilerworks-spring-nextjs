package com.boilerworks.api.repository;

import com.boilerworks.api.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

  Optional<Category> findBySlug(String slug);

  boolean existsBySlug(String slug);

  @Query("SELECT c FROM Category c WHERE " + "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))")
  List<Category> search(String search);

  List<Category> findAllByOrderBySortOrderAsc();
}
