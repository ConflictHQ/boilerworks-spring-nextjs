package com.boilerworks.api.controller;

import com.boilerworks.api.model.AppUser;
import com.boilerworks.api.model.Permission;
import com.boilerworks.api.model.UserGroup;
import com.boilerworks.api.repository.AppUserRepository;
import com.boilerworks.api.repository.PermissionRepository;
import com.boilerworks.api.repository.UserGroupRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AppUserRepository userRepository;
    @Autowired private UserGroupRepository groupRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        groupRepository.deleteAll();
        permissionRepository.deleteAll();

        Permission viewProducts = new Permission("products.view", "View Products", "Can view products");
        permissionRepository.save(viewProducts);

        UserGroup admins = new UserGroup("Administrators", "Full access");
        admins.setPermissions(Set.of(viewProducts));
        groupRepository.save(admins);

        AppUser user = new AppUser();
        user.setEmail("test@boilerworks.dev");
        user.setPassword(passwordEncoder.encode("testpass123"));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        user.setGroups(Set.of(admins));
        userRepository.save(user);
    }

    @Test
    void loginWithValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "test@boilerworks.dev",
                    "password", "testpass123"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.email").value("test@boilerworks.dev"))
            .andExpect(jsonPath("$.data.firstName").value("Test"));
    }

    @Test
    void loginWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "test@boilerworks.dev",
                    "password", "wrongpassword"
                ))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void meEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void meEndpointReturnsUserAfterLogin() throws Exception {
        var session = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "test@boilerworks.dev",
                    "password", "testpass123"
                ))))
            .andReturn().getRequest().getSession();

        mockMvc.perform(get("/api/auth/me").session((org.springframework.mock.web.MockHttpSession) session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.email").value("test@boilerworks.dev"))
            .andExpect(jsonPath("$.data.permissions").isArray());
    }
}
