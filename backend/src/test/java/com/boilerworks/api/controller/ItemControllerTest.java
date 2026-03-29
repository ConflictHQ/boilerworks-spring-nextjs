package com.boilerworks.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.boilerworks.api.model.AppUser;
import com.boilerworks.api.model.Permission;
import com.boilerworks.api.model.UserGroup;
import com.boilerworks.api.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private AppUserRepository userRepository;
  @Autowired private UserGroupRepository groupRepository;
  @Autowired private PermissionRepository permissionRepository;
  @Autowired private ItemRepository itemRepository;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private MockHttpSession adminSession;
  private MockHttpSession viewerSession;

  @BeforeEach
  void setUp() throws Exception {
    itemRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();
    groupRepository.deleteAll();
    permissionRepository.deleteAll();

    Permission viewItems =
        permissionRepository.save(new Permission("items.view", "View Items", ""));
    Permission createItems =
        permissionRepository.save(new Permission("items.create", "Create Items", ""));
    Permission editItems =
        permissionRepository.save(new Permission("items.edit", "Edit Items", ""));
    Permission deleteItems =
        permissionRepository.save(new Permission("items.delete", "Delete Items", ""));

    UserGroup admins = new UserGroup("Administrators", "Full access");
    admins.setPermissions(Set.of(viewItems, createItems, editItems, deleteItems));
    groupRepository.save(admins);

    UserGroup viewers = new UserGroup("Viewers", "Read-only");
    viewers.setPermissions(Set.of(viewItems));
    groupRepository.save(viewers);

    AppUser admin = new AppUser();
    admin.setEmail("admin@test.dev");
    admin.setPassword(passwordEncoder.encode("admin123"));
    admin.setFirstName("Admin");
    admin.setLastName("User");
    admin.setActive(true);
    admin.setStaff(true);
    admin.setGroups(Set.of(admins));
    userRepository.save(admin);

    AppUser viewer = new AppUser();
    viewer.setEmail("viewer@test.dev");
    viewer.setPassword(passwordEncoder.encode("viewer123"));
    viewer.setFirstName("Viewer");
    viewer.setLastName("User");
    viewer.setActive(true);
    viewer.setGroups(Set.of(viewers));
    userRepository.save(viewer);

    adminSession =
        (MockHttpSession)
            mockMvc
                .perform(
                    post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of("email", "admin@test.dev", "password", "admin123"))))
                .andReturn()
                .getRequest()
                .getSession();

    viewerSession =
        (MockHttpSession)
            mockMvc
                .perform(
                    post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of("email", "viewer@test.dev", "password", "viewer123"))))
                .andReturn()
                .getRequest()
                .getSession();
  }

  @Test
  void adminCanCreateItem() throws Exception {
    mockMvc
        .perform(
            post("/api/items")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "name", "Test Item",
                            "slug", "test-item",
                            "price", 29.99,
                            "sku", "TP-001",
                            "active", true))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.name").value("Test Item"))
        .andExpect(jsonPath("$.data.sku").value("TP-001"));
  }

  @Test
  void viewerCannotCreateItem() throws Exception {
    mockMvc
        .perform(
            post("/api/items")
                .session(viewerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "name", "Test Item",
                            "slug", "test-item",
                            "price", 29.99,
                            "sku", "TP-001"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void viewerCanListItems() throws Exception {
    mockMvc
        .perform(get("/api/items").session(viewerSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void adminCanDeleteItem() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/items")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "name", "Delete Me",
                                "slug", "delete-me",
                                "price", 9.99,
                                "sku", "DM-001"))))
            .andReturn();

    String id =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(delete("/api/items/" + id).session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true));

    // Item should not appear in list (soft deleted)
    mockMvc
        .perform(get("/api/items").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void adminCanGetItemById() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/items")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "name",
                                "Fetchable",
                                "slug",
                                "fetchable",
                                "price",
                                5.00,
                                "sku",
                                "F-001"))))
            .andReturn();

    String id =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(get("/api/items/" + id).session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.name").value("Fetchable"));
  }

  @Test
  void adminCanUpdateItem() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/items")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "name",
                                "Before Update",
                                "slug",
                                "before-update",
                                "price",
                                10.00,
                                "sku",
                                "BU-001"))))
            .andReturn();

    String id =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(
            put("/api/items/" + id)
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "name",
                            "After Update",
                            "slug",
                            "after-update",
                            "price",
                            20.00,
                            "sku",
                            "AU-001"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.name").value("After Update"))
        .andExpect(jsonPath("$.data.price").value(20.00));
  }

  @Test
  void viewerCannotDeleteItem() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/items")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "name",
                                "No Delete",
                                "slug",
                                "no-delete",
                                "price",
                                1.00,
                                "sku",
                                "ND-001"))))
            .andReturn();

    String id =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(delete("/api/items/" + id).session(viewerSession))
        .andExpect(status().isForbidden());
  }

  @Test
  void unauthenticatedCannotAccessItems() throws Exception {
    mockMvc.perform(get("/api/items")).andExpect(status().isUnauthorized());
  }
}
