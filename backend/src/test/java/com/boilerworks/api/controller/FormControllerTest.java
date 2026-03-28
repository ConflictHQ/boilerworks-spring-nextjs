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
class FormControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private AppUserRepository userRepository;
  @Autowired private UserGroupRepository groupRepository;
  @Autowired private PermissionRepository permissionRepository;
  @Autowired private FormDefinitionRepository formDefinitionRepository;
  @Autowired private FormSubmissionRepository formSubmissionRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private MockHttpSession adminSession;
  private MockHttpSession viewerSession;

  @BeforeEach
  void setUp() throws Exception {
    formSubmissionRepository.deleteAll();
    formDefinitionRepository.deleteAll();
    productRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();
    groupRepository.deleteAll();
    permissionRepository.deleteAll();

    Permission viewForms =
        permissionRepository.save(new Permission("forms.view", "View Forms", ""));
    Permission createForms =
        permissionRepository.save(new Permission("forms.create", "Create Forms", ""));
    Permission editForms =
        permissionRepository.save(new Permission("forms.edit", "Edit Forms", ""));
    Permission deleteForms =
        permissionRepository.save(new Permission("forms.delete", "Delete Forms", ""));
    Permission submitForms =
        permissionRepository.save(new Permission("forms.submit", "Submit Forms", ""));

    UserGroup admins = new UserGroup("Administrators", "Full access");
    admins.setPermissions(Set.of(viewForms, createForms, editForms, deleteForms, submitForms));
    groupRepository.save(admins);

    UserGroup viewers = new UserGroup("Viewers", "Read-only");
    viewers.setPermissions(Set.of(viewForms));
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
  void canCreateFormDefinition() throws Exception {
    mockMvc
        .perform(
            post("/api/forms")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "name", "Contact Form",
                            "slug", "contact-form",
                            "description", "A simple contact form",
                            "schemaJson",
                                Map.of(
                                    "fields",
                                    java.util.List.of(
                                        Map.of("name", "email", "type", "email"),
                                        Map.of("name", "message", "type", "textarea"))),
                            "active", true))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.name").value("Contact Form"))
        .andExpect(jsonPath("$.data.slug").value("contact-form"));
  }

  @Test
  void canListFormDefinitions() throws Exception {
    mockMvc
        .perform(get("/api/forms").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void canGetFormDefinitionById() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/forms")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("name", "Feedback", "slug", "feedback"))))
            .andReturn();

    String id =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(get("/api/forms/" + id).session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.name").value("Feedback"));
  }

  @Test
  void canSubmitForm() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/forms")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "name", "Survey",
                                "slug", "survey",
                                "schemaJson",
                                    Map.of(
                                        "fields",
                                        java.util.List.of(
                                            Map.of("name", "rating", "type", "number")))))))
            .andReturn();

    String formId =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(
            post("/api/forms/" + formId + "/submissions")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("dataJson", Map.of("rating", 5)))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.dataJson.rating").value(5));
  }

  @Test
  void canListSubmissions() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/forms")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(Map.of("name", "Poll", "slug", "poll"))))
            .andReturn();

    String formId =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(get("/api/forms/" + formId + "/submissions").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void viewerCannotCreateForm() throws Exception {
    mockMvc
        .perform(
            post("/api/forms")
                .session(viewerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("name", "Blocked", "slug", "blocked"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void viewerCannotDeleteForm() throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/forms")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("name", "Protected", "slug", "protected-form"))))
            .andReturn();

    String id =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(delete("/api/forms/" + id).session(viewerSession))
        .andExpect(status().isForbidden());
  }

  @Test
  void duplicateSlugReturnsError() throws Exception {
    mockMvc.perform(
        post("/api/forms")
            .session(adminSession)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                objectMapper.writeValueAsString(Map.of("name", "First", "slug", "same-slug"))));

    mockMvc
        .perform(
            post("/api/forms")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("name", "Second", "slug", "same-slug"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.ok").value(false));
  }

  @Test
  void unauthenticatedCannotAccessForms() throws Exception {
    mockMvc.perform(get("/api/forms")).andExpect(status().isUnauthorized());
  }
}
