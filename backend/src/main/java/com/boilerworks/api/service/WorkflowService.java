package com.boilerworks.api.service;

import com.boilerworks.api.dto.WorkflowDefinitionRequest;
import com.boilerworks.api.dto.WorkflowTransitionRequest;
import com.boilerworks.api.model.WorkflowDefinition;
import com.boilerworks.api.model.WorkflowInstance;
import com.boilerworks.api.model.WorkflowTransitionLog;
import com.boilerworks.api.repository.WorkflowDefinitionRepository;
import com.boilerworks.api.repository.WorkflowInstanceRepository;
import com.boilerworks.api.security.BoilerworksUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;

    @Transactional(readOnly = true)
    public List<WorkflowDefinition> findAllDefinitions() {
        return workflowDefinitionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public WorkflowDefinition findDefinitionById(UUID id) {
        return workflowDefinitionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Workflow definition not found: " + id));
    }

    @Transactional(readOnly = true)
    public WorkflowDefinition findDefinitionBySlug(String slug) {
        return workflowDefinitionRepository.findBySlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Workflow definition not found: " + slug));
    }

    @Transactional
    public WorkflowDefinition createDefinition(WorkflowDefinitionRequest request) {
        if (workflowDefinitionRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Workflow with slug already exists: " + request.getSlug());
        }

        WorkflowDefinition wf = new WorkflowDefinition();
        wf.setName(request.getName());
        wf.setSlug(request.getSlug());
        wf.setDescription(request.getDescription());
        wf.setStatesJson(request.getStatesJson());
        wf.setTransitionsJson(request.getTransitionsJson());
        wf.setInitialState(request.getInitialState());
        wf.setActive(request.isActive());
        return workflowDefinitionRepository.save(wf);
    }

    @Transactional
    public WorkflowDefinition updateDefinition(UUID id, WorkflowDefinitionRequest request) {
        WorkflowDefinition wf = findDefinitionById(id);
        wf.setName(request.getName());
        wf.setSlug(request.getSlug());
        wf.setDescription(request.getDescription());
        wf.setStatesJson(request.getStatesJson());
        wf.setTransitionsJson(request.getTransitionsJson());
        wf.setInitialState(request.getInitialState());
        wf.setActive(request.isActive());
        wf.setVersion(wf.getVersion() + 1);
        return workflowDefinitionRepository.save(wf);
    }

    @Transactional
    public void softDeleteDefinition(UUID id, BoilerworksUserDetails currentUser) {
        WorkflowDefinition wf = findDefinitionById(id);
        wf.softDelete(currentUser.getUserId());
        workflowDefinitionRepository.save(wf);
    }

    @Transactional
    public WorkflowInstance startInstance(UUID definitionId) {
        WorkflowDefinition wf = findDefinitionById(definitionId);

        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflowDefinition(wf);
        instance.setCurrentState(wf.getInitialState());
        instance.setContextJson(new HashMap<>());
        instance.setStatus(WorkflowInstance.InstanceStatus.ACTIVE);
        return workflowInstanceRepository.save(instance);
    }

    @Transactional(readOnly = true)
    public WorkflowInstance findInstanceById(UUID id) {
        return workflowInstanceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Workflow instance not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<WorkflowInstance> findInstancesByDefinition(UUID definitionId) {
        return workflowInstanceRepository.findByWorkflowDefinitionId(definitionId);
    }

    @Transactional
    public WorkflowInstance executeTransition(UUID instanceId, WorkflowTransitionRequest request,
                                              BoilerworksUserDetails currentUser) {
        WorkflowInstance instance = findInstanceById(instanceId);

        if (instance.getStatus() != WorkflowInstance.InstanceStatus.ACTIVE) {
            throw new IllegalStateException("Workflow instance is not active");
        }

        WorkflowDefinition definition = instance.getWorkflowDefinition();
        List<Map<String, Object>> transitions = definition.getTransitionsJson();

        Map<String, Object> matchedTransition = null;
        if (transitions != null) {
            for (Map<String, Object> t : transitions) {
                if (request.getTransitionName().equals(t.get("name"))
                    && instance.getCurrentState().equals(t.get("from"))) {
                    matchedTransition = t;
                    break;
                }
            }
        }

        if (matchedTransition == null) {
            throw new IllegalArgumentException(
                "No valid transition '" + request.getTransitionName() +
                "' from state '" + instance.getCurrentState() + "'");
        }

        String fromState = instance.getCurrentState();
        String toState = (String) matchedTransition.get("to");

        WorkflowTransitionLog log = new WorkflowTransitionLog();
        log.setWorkflowInstance(instance);
        log.setFromState(fromState);
        log.setToState(toState);
        log.setTransitionName(request.getTransitionName());
        log.setTriggeredBy(currentUser != null ? currentUser.getUserId() : null);
        log.setTriggeredAt(Instant.now());
        log.setMetadataJson(request.getMetadata());

        instance.getTransitionLogs().add(log);
        instance.setCurrentState(toState);

        List<Map<String, Object>> states = definition.getStatesJson();
        if (states != null) {
            for (Map<String, Object> s : states) {
                if (toState.equals(s.get("name")) && Boolean.TRUE.equals(s.get("isFinal"))) {
                    instance.setStatus(WorkflowInstance.InstanceStatus.COMPLETED);
                    break;
                }
            }
        }

        return workflowInstanceRepository.save(instance);
    }
}
