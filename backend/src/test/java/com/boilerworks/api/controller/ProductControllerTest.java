package com.boilerworks.api.controller;

import com.boilerworks.api.model.AppUser;
import com.boilerworks.api.model.Category;
import com.boilerworks.api.model.Permission;
import com.boilerworks.api.model.UserGroup;
import com.boilerworks.api.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AppUserRepository userRepository;
    @Autowired private UserGroupRepository groupRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
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

        Permission viewProducts = permissionRepository.save(new Permission("products.view", "View Products", ""));
        Permission createProducts = permissionRepository.save(new Permission("products.create", "Create Products", ""));
        Permission editProducts = permissionRepository.save(new Permission("products.edit", "Edit Products", ""));
        Permission deleteProducts = permissionRepository.save(new Permission("products.delete", "Delete Products", ""));

        UserGroup admins = new UserGroup("Administrators", "Full access");
        admins.setPermissions(Set.of(viewProducts, createProducts, editProducts, deleteProducts));
        groupRepository.save(admins);

        UserGroup viewers = new UserGroup("Viewers", "Read-only");
        viewers.setPermissions(Set.of(viewProducts));
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

        adminSession = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "admin@test.dev", "password", "admin123"))))
            .andReturn().getRequest().getSession();

        viewerSession = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "viewer@test.dev", "password", "viewer123"))))
            .andReturn().getRequest().getSession();
    }

    @Test
    void adminCanCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Test Product",
                    "slug", "test-product",
                    "price", 29.99,
                    "sku", "TP-001",
                    "active", true
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.name").value("Test Product"))
            .andExpect(jsonPath("$.data.sku").value("TP-001"));
    }

    @Test
    void viewerCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                .session(viewerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Test Product",
                    "slug", "test-product",
                    "price", 29.99,
                    "sku", "TP-001"
                ))))
            .andExpect(status().isForbidden());
    }

    @Test
    void viewerCanListProducts() throws Exception {
        mockMvc.perform(get("/api/products").session(viewerSession))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void adminCanDeleteProduct() throws Exception {
        var result = mockMvc.perform(post("/api/products")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Delete Me",
                    "slug", "delete-me",
                    "price", 9.99,
                    "sku", "DM-001"
                ))))
            .andReturn();

        String id = objectMapper.readTree(result.getResponse().getContentAsString())
            .path("data").path("id").asText();

        mockMvc.perform(delete("/api/products/" + id).session(adminSession))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true));

        // Product should not appear in list (soft deleted)
        mockMvc.perform(get("/api/products").session(adminSession))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void unauthenticatedCannotAccessProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isUnauthorized());
    }
}
