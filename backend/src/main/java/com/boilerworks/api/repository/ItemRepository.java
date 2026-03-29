package com.boilerworks.api.repository;

import com.boilerworks.api.model.Item;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

  Optional<Item> findBySlug(String slug);

  boolean existsBySlug(String slug);

  boolean existsBySku(String sku);

  @Query(
      "SELECT p FROM Item p WHERE "
          + "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR "
          + "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))")
  List<Item> search(String search);
}
