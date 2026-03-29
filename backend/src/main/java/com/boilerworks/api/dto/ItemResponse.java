package com.boilerworks.api.dto;

import com.boilerworks.api.model.Item;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ItemResponse {

  private final UUID id;
  private final String name;
  private final String slug;
  private final String description;
  private final BigDecimal price;
  private final String sku;
  private final boolean active;
  private final UUID categoryId;
  private final String categoryName;
  private final Instant createdAt;
  private final Instant updatedAt;

  public ItemResponse(Item item) {
    this.id = item.getId();
    this.name = item.getName();
    this.slug = item.getSlug();
    this.description = item.getDescription();
    this.price = item.getPrice();
    this.sku = item.getSku();
    this.active = item.isActive();
    this.categoryId = item.getCategory() != null ? item.getCategory().getId() : null;
    this.categoryName = item.getCategory() != null ? item.getCategory().getName() : null;
    this.createdAt = item.getCreatedAt();
    this.updatedAt = item.getUpdatedAt();
  }
}
