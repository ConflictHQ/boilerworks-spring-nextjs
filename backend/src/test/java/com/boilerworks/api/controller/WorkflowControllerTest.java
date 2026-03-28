package com.boilerworks.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.boilerworks.api.model.AppUser;
import com.boilerworks.api.model.Permission;
import com.boilerworks.api.model.UserGroup;
import com.boilerworks.api.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
class WorkflowControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private AppUserRepository userRepository;
  @Autowired private UserGroupRepository groupRepository;
  @Autowired private PermissionRepository permissionRepository;
  @Autowired private WorkflowDefinitionRepository workflowDefinitionRepository;
  @Autowired private WorkflowInstanceRepository workflowInstanceRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private MockHttpSession adminSession;
  private MockHttpSession viewerSession;

  @BeforeEach
  void setUp() throws Exception {
    workflowInstanceRepository.deleteAll();
    workflowDefinitionRepository.deleteAll();
    productRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();
    groupRepository.deleteAll();
    permissionRepository.deleteAll();

    Permission viewWf =
        permissionRepository.save(new Permission("workflows.view", "View Workflows", ""));
    Permission createWf =
        permissionRepository.save(new Permission("workflows.create", "Create Workflows", ""));
    Permission editWf =
        permissionRepository.save(new Permission("workflows.edit", "Edit Workflows", ""));
    Permission deleteWf =
        permissionRepository.save(new Permission("workflows.delete", "Delete Workflows", ""));
    Permission executeWf =
        permissionRepository.save(new Permission("workflows.execute", "Execute Workflows", ""));

    UserGroup admins = new UserGroup("Administrators", "Full access");
    admins.setPermissions(Set.of(viewWf, createWf, editWf, deleteWf, executeWf));
    groupRepository.save(admins);

    UserGroup viewers = new UserGroup("Viewers", "Read-only");
    viewers.setPermissions(Set.of(viewWf));
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

  private String createWorkflowDefinition(String name, String slug) throws Exception {
    var result =
        mockMvc
            .perform(
                post("/api/workflows")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "name",
                                name,
                                "slug",
                                slug,
                                "initialState",
                                "draft",
                                "statesJson",
                                List.of(
                                    Map.of("name", "draft", "label", "Draft"),
                                    Map.of("name", "review", "label", "Review"),
                                    Map.of("name", "done", "label", "Done", "isFinal", true)),
                                "transitionsJson",
                                List.of(
                                    Map.of("name", "submit", "from", "draft", "to", "review"),
                                    Map.of("name", "approve", "from", "review", "to", "done"))))))
            .andReturn();

    return objectMapper
        .readTree(result.getResponse().getContentAsString())
        .path("data")
        .path("id")
        .asText();
  }

  @Test
  void canCreateWorkflowDefinition() throws Exception {
    mockMvc
        .perform(
            post("/api/workflows")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "name", "Approval",
                            "slug", "approval",
                            "initialState", "draft",
                            "statesJson",
                                List.of(
                                    Map.of("name", "draft", "label", "Draft"),
                                    Map.of("name", "done", "label", "Done", "isFinal", true)),
                            "transitionsJson",
                                List.of(Map.of("name", "finish", "from", "draft", "to", "done"))))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.name").value("Approval"))
        .andExpect(jsonPath("$.data.initialState").value("draft"));
  }

  @Test
  void canListWorkflowDefinitions() throws Exception {
    mockMvc
        .perform(get("/api/workflows").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void canGetWorkflowDefinitionById() throws Exception {
    String id = createWorkflowDefinition("Fetch Me", "fetch-me");

    mockMvc
        .perform(get("/api/workflows/" + id).session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.name").value("Fetch Me"));
  }

  @Test
  void canStartWorkflowInstance() throws Exception {
    String defId = createWorkflowDefinition("Start Test", "start-test");

    mockMvc
        .perform(post("/api/workflows/" + defId + "/instances").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.currentState").value("draft"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
  }

  @Test
  void canTransitionWorkflowInstance() throws Exception {
    String defId = createWorkflowDefinition("Transition Test", "transition-test");

    var instanceResult =
        mockMvc
            .perform(post("/api/workflows/" + defId + "/instances").session(adminSession))
            .andReturn();

    String instanceId =
        objectMapper
            .readTree(instanceResult.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(
            post("/api/workflows/instances/" + instanceId + "/transition")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("transitionName", "submit"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.currentState").value("review"));

    mockMvc
        .perform(
            post("/api/workflows/instances/" + instanceId + "/transition")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("transitionName", "approve"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data.currentState").value("done"))
        .andExpect(jsonPath("$.data.status").value("COMPLETED"));
  }

  @Test
  void canListWorkflowInstances() throws Exception {
    String defId = createWorkflowDefinition("List Instances", "list-instances");

    mockMvc.perform(post("/api/workflows/" + defId + "/instances").session(adminSession));

    mockMvc
        .perform(get("/api/workflows/" + defId + "/instances").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  void viewerCannotCreateWorkflow() throws Exception {
    mockMvc
        .perform(
            post("/api/workflows")
                .session(viewerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "name", "Blocked",
                            "slug", "blocked",
                            "initialState", "draft",
                            "statesJson", List.of(Map.of("name", "draft", "label", "Draft")),
                            "transitionsJson", List.of()))))
        .andExpect(status().isForbidden());
  }

  @Test
  void viewerCannotStartInstance() throws Exception {
    String defId = createWorkflowDefinition("No Start", "no-start");

    mockMvc
        .perform(post("/api/workflows/" + defId + "/instances").session(viewerSession))
        .andExpect(status().isForbidden());
  }

  @Test
  void viewerCannotTransition() throws Exception {
    String defId = createWorkflowDefinition("No Transition", "no-transition");

    var instanceResult =
        mockMvc
            .perform(post("/api/workflows/" + defId + "/instances").session(adminSession))
            .andReturn();

    String instanceId =
        objectMapper
            .readTree(instanceResult.getResponse().getContentAsString())
            .path("data")
            .path("id")
            .asText();

    mockMvc
        .perform(
            post("/api/workflows/instances/" + instanceId + "/transition")
                .session(viewerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("transitionName", "submit"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void unauthenticatedCannotAccessWorkflows() throws Exception {
    mockMvc.perform(get("/api/workflows")).andExpect(status().isUnauthorized());
  }
}
