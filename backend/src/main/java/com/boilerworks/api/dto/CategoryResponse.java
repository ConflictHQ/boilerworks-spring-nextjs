package com.boilerworks.api.dto;

import com.boilerworks.api.model.Category;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class CategoryResponse {

    private final UUID id;
    private final String name;
    private final String slug;
    private final String description;
    private final int sortOrder;
    private final Instant createdAt;
    private final Instant updatedAt;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.slug = category.getSlug();
        this.description = category.getDescription();
        this.sortOrder = category.getSortOrder();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
    }
}
