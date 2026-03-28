package com.boilerworks.api.service;

import com.boilerworks.api.dto.CategoryRequest;
import com.boilerworks.api.model.Category;
import com.boilerworks.api.repository.CategoryRepository;
import com.boilerworks.api.security.BoilerworksUserDetails;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional(readOnly = true)
  public List<Category> findAll(String search) {
    if (search != null && !search.isBlank()) {
      return categoryRepository.search(search.trim());
    }
    return categoryRepository.findAllByOrderBySortOrderAsc();
  }

  @Transactional(readOnly = true)
  public Category findById(UUID id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
  }

  @Transactional(readOnly = true)
  public Category findBySlug(String slug) {
    return categoryRepository
        .findBySlug(slug)
        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + slug));
  }

  @Transactional
  public Category create(CategoryRequest request) {
    if (categoryRepository.existsBySlug(request.getSlug())) {
      throw new IllegalArgumentException("Category with slug already exists: " + request.getSlug());
    }

    Category category = new Category();
    mapRequestToCategory(request, category);
    return categoryRepository.save(category);
  }

  @Transactional
  public Category update(UUID id, CategoryRequest request) {
    Category category = findById(id);
    mapRequestToCategory(request, category);
    return categoryRepository.save(category);
  }

  @Transactional
  public void softDelete(UUID id, BoilerworksUserDetails currentUser) {
    Category category = findById(id);
    category.softDelete(currentUser.getUserId());
    categoryRepository.save(category);
  }

  private void mapRequestToCategory(CategoryRequest request, Category category) {
    category.setName(request.getName());
    category.setSlug(request.getSlug());
    category.setDescription(request.getDescription());
    category.setSortOrder(request.getSortOrder());
  }
}
