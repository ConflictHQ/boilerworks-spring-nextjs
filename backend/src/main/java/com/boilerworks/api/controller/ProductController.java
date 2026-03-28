package com.boilerworks.api.controller;

import com.boilerworks.api.dto.ApiResponse;
import com.boilerworks.api.dto.ProductRequest;
import com.boilerworks.api.dto.ProductResponse;
import com.boilerworks.api.model.Product;
import com.boilerworks.api.security.BoilerworksUserDetails;
import com.boilerworks.api.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping
  @PreAuthorize("hasAuthority('products.view')")
  public ResponseEntity<ApiResponse<List<ProductResponse>>> list(
      @RequestParam(required = false) String search) {
    List<ProductResponse> products =
        productService.findAll(search).stream().map(ProductResponse::new).toList();
    return ResponseEntity.ok(ApiResponse.ok(products));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('products.view')")
  public ResponseEntity<ApiResponse<ProductResponse>> get(@PathVariable UUID id) {
    Product product = productService.findById(id);
    return ResponseEntity.ok(ApiResponse.ok(new ProductResponse(product)));
  }

  @PostMapping
  @PreAuthorize("hasAuthority('products.create')")
  public ResponseEntity<ApiResponse<ProductResponse>> create(
      @Valid @RequestBody ProductRequest request) {
    try {
      Product product = productService.create(request);
      return ResponseEntity.ok(ApiResponse.ok(new ProductResponse(product)));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('products.edit')")
  public ResponseEntity<ApiResponse<ProductResponse>> update(
      @PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
    try {
      Product product = productService.update(id, request);
      return ResponseEntity.ok(ApiResponse.ok(new ProductResponse(product)));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('products.delete')")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable UUID id, @AuthenticationPrincipal BoilerworksUserDetails user) {
    try {
      productService.softDelete(id, user);
      return ResponseEntity.ok(ApiResponse.ok());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }
}
