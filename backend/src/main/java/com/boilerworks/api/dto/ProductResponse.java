package com.boilerworks.api.dto;

import com.boilerworks.api.model.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ProductResponse {

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

  public ProductResponse(Product product) {
    this.id = product.getId();
    this.name = product.getName();
    this.slug = product.getSlug();
    this.description = product.getDescription();
    this.price = product.getPrice();
    this.sku = product.getSku();
    this.active = product.isActive();
    this.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
    this.categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
    this.createdAt = product.getCreatedAt();
    this.updatedAt = product.getUpdatedAt();
  }
}
