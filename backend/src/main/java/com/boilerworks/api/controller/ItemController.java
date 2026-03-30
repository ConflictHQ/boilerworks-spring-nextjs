package com.boilerworks.api.controller;

import com.boilerworks.api.dto.ApiResponse;
import com.boilerworks.api.dto.ItemRequest;
import com.boilerworks.api.dto.ItemResponse;
import com.boilerworks.api.model.Item;
import com.boilerworks.api.security.BoilerworksUserDetails;
import com.boilerworks.api.service.ItemService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  @GetMapping
  @PreAuthorize("hasAuthority('items.view')")
  public ResponseEntity<ApiResponse<List<ItemResponse>>> list(
      @RequestParam(required = false) String search) {
    List<ItemResponse> items = itemService.findAll(search).stream().map(ItemResponse::new).toList();
    return ResponseEntity.ok(ApiResponse.ok(items));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('items.view')")
  public ResponseEntity<ApiResponse<ItemResponse>> get(@PathVariable UUID id) {
    Item item = itemService.findById(id);
    return ResponseEntity.ok(ApiResponse.ok(new ItemResponse(item)));
  }

  @PostMapping
  @PreAuthorize("hasAuthority('items.create')")
  public ResponseEntity<ApiResponse<ItemResponse>> create(@Valid @RequestBody ItemRequest request) {
    try {
      Item item = itemService.create(request);
      return ResponseEntity.ok(ApiResponse.ok(new ItemResponse(item)));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('items.edit')")
  public ResponseEntity<ApiResponse<ItemResponse>> update(
      @PathVariable UUID id, @Valid @RequestBody ItemRequest request) {
    try {
      Item item = itemService.update(id, request);
      return ResponseEntity.ok(ApiResponse.ok(new ItemResponse(item)));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('items.delete')")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable UUID id, @AuthenticationPrincipal BoilerworksUserDetails user) {
    try {
      itemService.softDelete(id, user);
      return ResponseEntity.ok(ApiResponse.ok());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }
}
