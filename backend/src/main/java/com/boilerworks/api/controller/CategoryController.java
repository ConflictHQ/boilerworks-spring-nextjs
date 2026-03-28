package com.boilerworks.api.controller;

import com.boilerworks.api.dto.ApiResponse;
import com.boilerworks.api.dto.CategoryRequest;
import com.boilerworks.api.dto.CategoryResponse;
import com.boilerworks.api.model.Category;
import com.boilerworks.api.security.BoilerworksUserDetails;
import com.boilerworks.api.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasAuthority('categories.view')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list(
            @RequestParam(required = false) String search) {
        List<CategoryResponse> categories = categoryService.findAll(search).stream()
            .map(CategoryResponse::new)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('categories.view')")
    public ResponseEntity<ApiResponse<CategoryResponse>> get(@PathVariable UUID id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(new CategoryResponse(category)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('categories.create')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.create(request);
            return ResponseEntity.ok(ApiResponse.ok(new CategoryResponse(category)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('categories.edit')")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable UUID id,
                                                                @Valid @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.update(id, request);
            return ResponseEntity.ok(ApiResponse.ok(new CategoryResponse(category)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('categories.delete')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id,
                                                    @AuthenticationPrincipal BoilerworksUserDetails user) {
        try {
            categoryService.softDelete(id, user);
            return ResponseEntity.ok(ApiResponse.ok());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
