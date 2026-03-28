package com.boilerworks.api.service;

import com.boilerworks.api.dto.ProductRequest;
import com.boilerworks.api.model.Category;
import com.boilerworks.api.model.Product;
import com.boilerworks.api.repository.CategoryRepository;
import com.boilerworks.api.repository.ProductRepository;
import com.boilerworks.api.security.BoilerworksUserDetails;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  @Transactional(readOnly = true)
  public List<Product> findAll(String search) {
    if (search != null && !search.isBlank()) {
      return productRepository.search(search.trim());
    }
    return productRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Product findById(UUID id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
  }

  @Transactional(readOnly = true)
  public Product findBySlug(String slug) {
    return productRepository
        .findBySlug(slug)
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + slug));
  }

  @Transactional
  public Product create(ProductRequest request) {
    if (productRepository.existsBySlug(request.getSlug())) {
      throw new IllegalArgumentException("Product with slug already exists: " + request.getSlug());
    }
    if (productRepository.existsBySku(request.getSku())) {
      throw new IllegalArgumentException("Product with SKU already exists: " + request.getSku());
    }

    Product product = new Product();
    mapRequestToProduct(request, product);
    return productRepository.save(product);
  }

  @Transactional
  public Product update(UUID id, ProductRequest request) {
    Product product = findById(id);
    mapRequestToProduct(request, product);
    return productRepository.save(product);
  }

  @Transactional
  public void softDelete(UUID id, BoilerworksUserDetails currentUser) {
    Product product = findById(id);
    product.softDelete(currentUser.getUserId());
    productRepository.save(product);
  }

  private void mapRequestToProduct(ProductRequest request, Product product) {
    product.setName(request.getName());
    product.setSlug(request.getSlug());
    product.setDescription(request.getDescription());
    product.setPrice(request.getPrice());
    product.setSku(request.getSku());
    product.setActive(request.isActive());

    if (request.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(request.getCategoryId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Category not found: " + request.getCategoryId()));
      product.setCategory(category);
    } else {
      product.setCategory(null);
    }
  }
}
