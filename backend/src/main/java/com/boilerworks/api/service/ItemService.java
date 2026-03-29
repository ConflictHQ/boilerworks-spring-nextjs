package com.boilerworks.api.service;

import com.boilerworks.api.dto.ItemRequest;
import com.boilerworks.api.model.Category;
import com.boilerworks.api.model.Item;
import com.boilerworks.api.repository.CategoryRepository;
import com.boilerworks.api.repository.ItemRepository;
import com.boilerworks.api.security.BoilerworksUserDetails;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;
  private final CategoryRepository categoryRepository;

  @Transactional(readOnly = true)
  public List<Item> findAll(String search) {
    if (search != null && !search.isBlank()) {
      return itemRepository.search(search.trim());
    }
    return itemRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Item findById(UUID id) {
    return itemRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Item not found: " + id));
  }

  @Transactional(readOnly = true)
  public Item findBySlug(String slug) {
    return itemRepository
        .findBySlug(slug)
        .orElseThrow(() -> new IllegalArgumentException("Item not found: " + slug));
  }

  @Transactional
  public Item create(ItemRequest request) {
    if (itemRepository.existsBySlug(request.getSlug())) {
      throw new IllegalArgumentException("Item with slug already exists: " + request.getSlug());
    }
    if (itemRepository.existsBySku(request.getSku())) {
      throw new IllegalArgumentException("Item with SKU already exists: " + request.getSku());
    }

    Item item = new Item();
    mapRequestToItem(request, item);
    return itemRepository.save(item);
  }

  @Transactional
  public Item update(UUID id, ItemRequest request) {
    Item item = findById(id);
    mapRequestToItem(request, item);
    return itemRepository.save(item);
  }

  @Transactional
  public void softDelete(UUID id, BoilerworksUserDetails currentUser) {
    Item item = findById(id);
    item.softDelete(currentUser.getUserId());
    itemRepository.save(item);
  }

  private void mapRequestToItem(ItemRequest request, Item item) {
    item.setName(request.getName());
    item.setSlug(request.getSlug());
    item.setDescription(request.getDescription());
    item.setPrice(request.getPrice());
    item.setSku(request.getSku());
    item.setActive(request.isActive());

    if (request.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(request.getCategoryId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Category not found: " + request.getCategoryId()));
      item.setCategory(category);
    } else {
      item.setCategory(null);
    }
  }
}
