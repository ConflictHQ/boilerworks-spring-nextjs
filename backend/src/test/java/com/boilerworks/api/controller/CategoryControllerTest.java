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
class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private AppUserRepository userRepository;
  @Autowired private UserGroupRepository groupRepository;
  @Autowired private PermissionRepository permissionRepository;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private MockHttpSession adminSession;
  private MockHttpSession viewerSession;

  @BeforeEach
  void setUp() throws Exception {
    productRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();
    groupRepository.deleteAll();
    permissionRepository.deleteAll();

    Permission viewCat =
        permissionRepository.save(new Permission("categories.view", "View Categories", ""));
    Permission createCat =
        permissionRepository.save(new Permission("categories.create", "Create Categories", ""));
    Permission editCat =
        permissionRepository.save(new Permission("categories.edit", "Edit Categories", ""));
    Permission deleteCat =
        permissionRepository.save(new Permission("categories.delete", "Delete Categories", ""));

    UserGroup admins = new UserGroup("Administrators", "Full access");
    admins.setPermissions(Set.of(viewCat, createCat, editCat, deleteCat));
    groupRepository.save(admins);

    UserGroup viewers = new UserGroup("Viewers", "Read-only");
    viewers.setPermissions(Set.of(viewCat));
    groupRepository.save(viewers);

    AppUser admin = new AppUser();
    admin.setEmail("admin@test.dev");
    admin.setPassword(passwordEncoder.encode("admin123"));
    admin.setFirstName("Admin");
    admin.setLastName("User");
    admin.setActive(true);
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
  void canCreateCategory() throws Exception {
    mockMvc
        .perform(
            post("/api/categories")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "name", "Electronics",
                            "slug", "electronics",
                            "description", "Electronic devices",
                            "sortOrder", 1))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.name").value("Electronics"))
        .andExpect(jsonPath("$.data.slug").value("electronics"));
  }

  @Test
  void canListCategories() throws Exception {
    mockMvc
        .perform(get("/api/categories").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void canGetCategoryById() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/categories")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("name", "Clothing", "slug", "clothing", "sortOrder", 1))))
            .andReturn();

    String id =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(get("/api/categories/" + id).session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.name").value("Clothing"));
  }

  @Test
  void canUpdateCategory() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/categories")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("name", "Old Name", "slug", "old-name"))))
            .andReturn();

    String id =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(
            put("/api/categories/" + id)
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("name", "New Name", "slug", "new-name"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.name").value("New Name"));
  }

  @Test
  void canDeleteCategory() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/categories")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("name", "Removable", "slug", "removable"))))
            .andReturn();

    String id =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(delete("/api/categories/" + id).session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true));

    // Soft-deleted category should not appear in list
    mockMvc
        .perform(get("/api/categories").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void viewerCannotCreateCategory() throws Exception {
    mockMvc
        .perform(
            post("/api/categories")
                .session(viewerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("name", "Blocked", "slug", "blocked"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void viewerCannotDeleteCategory() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/categories")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("name", "Protected", "slug", "protected-cat"))))
            .andReturn();

    String id =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(delete("/api/categories/" + id).session(viewerSession))
        .andExpect(status().isForbidden());
  }

  @Test
  void duplicateSlugReturnsError() throws Exception {
    mockMvc.perform(
        post("/api/categories")
            .session(adminSession)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("name", "Books", "slug", "books"))));

    mockMvc
        .perform(
            post("/api/categories")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("name", "Books 2", "slug", "books"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.ok").value(false));
  }
}
