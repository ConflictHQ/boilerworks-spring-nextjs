package com.boilerworks.api.service;

import static org.junit.jupiter.api.Assertions.*;

import com.boilerworks.api.dto.WorkflowDefinitionRequest;
import com.boilerworks.api.dto.WorkflowTransitionRequest;
import com.boilerworks.api.model.WorkflowDefinition;
import com.boilerworks.api.model.WorkflowInstance;
import com.boilerworks.api.repository.WorkflowDefinitionRepository;
import com.boilerworks.api.repository.WorkflowInstanceRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorkflowServiceTest {

  @Autowired private WorkflowService workflowService;
  @Autowired private WorkflowDefinitionRepository definitionRepository;
  @Autowired private WorkflowInstanceRepository instanceRepository;

  @BeforeEach
  void setUp() {
    instanceRepository.deleteAll();
    definitionRepository.deleteAll();
  }

  @Test
  void canCreateAndStartWorkflow() {
    WorkflowDefinitionRequest request = new WorkflowDefinitionRequest();
    request.setName("Approval Flow");
    request.setSlug("approval-flow");
    request.setDescription("Simple approval workflow");
    request.setInitialState("draft");
    request.setStatesJson(
        List.of(
            Map.of("name", "draft", "label", "Draft"),
            Map.of("name", "pending", "label", "Pending Review"),
            Map.of("name", "approved", "label", "Approved", "isFinal", true),
            Map.of("name", "rejected", "label", "Rejected", "isFinal", true)));
    request.setTransitionsJson(
        List.of(
            Map.of("name", "submit", "from", "draft", "to", "pending"),
            Map.of("name", "approve", "from", "pending", "to", "approved"),
            Map.of("name", "reject", "from", "pending", "to", "rejected")));

    WorkflowDefinition definition = workflowService.createDefinition(request);
    assertNotNull(definition.getId());
    assertEquals("draft", definition.getInitialState());

    WorkflowInstance instance = workflowService.startInstance(definition.getId());
    assertNotNull(instance.getId());
    assertEquals("draft", instance.getCurrentState());
    assertEquals(WorkflowInstance.InstanceStatus.ACTIVE, instance.getStatus());
  }

  @Test
  void canExecuteTransitions() {
    WorkflowDefinitionRequest request = new WorkflowDefinitionRequest();
    request.setName("Test Flow");
    request.setSlug("test-flow");
    request.setInitialState("start");
    request.setStatesJson(
        List.of(
            Map.of("name", "start", "label", "Start"),
            Map.of("name", "end", "label", "End", "isFinal", true)));
    request.setTransitionsJson(List.of(Map.of("name", "complete", "from", "start", "to", "end")));

    WorkflowDefinition definition = workflowService.createDefinition(request);
    WorkflowInstance instance = workflowService.startInstance(definition.getId());

    WorkflowTransitionRequest transition = new WorkflowTransitionRequest();
    transition.setTransitionName("complete");

    WorkflowInstance updated =
        workflowService.executeTransition(instance.getId(), transition, null);
    assertEquals("end", updated.getCurrentState());
    assertEquals(WorkflowInstance.InstanceStatus.COMPLETED, updated.getStatus());
    assertFalse(updated.getTransitionLogs().isEmpty());
  }

  @Test
  void invalidTransitionThrows() {
    WorkflowDefinitionRequest request = new WorkflowDefinitionRequest();
    request.setName("No Path");
    request.setSlug("no-path");
    request.setInitialState("start");
    request.setStatesJson(List.of(Map.of("name", "start", "label", "Start")));
    request.setTransitionsJson(List.of());

    WorkflowDefinition definition = workflowService.createDefinition(request);
    WorkflowInstance instance = workflowService.startInstance(definition.getId());

    WorkflowTransitionRequest transition = new WorkflowTransitionRequest();
    transition.setTransitionName("nonexistent");

    assertThrows(
        IllegalArgumentException.class,
        () -> workflowService.executeTransition(instance.getId(), transition, null));
  }
}
