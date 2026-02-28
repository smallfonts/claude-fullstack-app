package com.deploytrack.service;

import com.deploytrack.dto.ActionDeploymentRequestDTO;
import com.deploytrack.dto.CreateDeploymentRequestDTO;
import com.deploytrack.dto.DeploymentRequestDTO;
import com.deploytrack.enums.DeploymentMethodType;
import com.deploytrack.enums.DeploymentStatus;
import com.deploytrack.enums.UserRole;
import com.deploytrack.model.ComponentArtifact;
import com.deploytrack.model.DeploymentMethod;
import com.deploytrack.model.DeploymentRequest;
import com.deploytrack.model.User;
import com.deploytrack.repository.ComponentArtifactRepository;
import com.deploytrack.repository.DeploymentMethodRepository;
import com.deploytrack.repository.DeploymentRequestRepository;
import com.deploytrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentRequestService {

    private final DeploymentRequestRepository deploymentRequestRepository;
    private final UserRepository userRepository;
    private final ComponentArtifactRepository componentArtifactRepository;
    private final DeploymentMethodRepository deploymentMethodRepository;
    private final UDeployService uDeployService;

    @Transactional
    public DeploymentRequestDTO createRequest(CreateDeploymentRequestDTO dto) {
        User requestor = userRepository.findById(dto.getRequestedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getRequestedByUserId()));

        if (requestor.getRole() == UserRole.READ_ONLY) {
            throw new IllegalStateException("Read-only users cannot create deployment requests.");
        }

        ComponentArtifact artifact = componentArtifactRepository.findById(dto.getComponentArtifactId())
                .orElseThrow(() -> new IllegalArgumentException("Artifact not found: " + dto.getComponentArtifactId()));

        DeploymentMethod method = deploymentMethodRepository.findById(dto.getDeploymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Deployment method not found: " + dto.getDeploymentMethodId()));

        // Find last deployment for this application
        DeploymentRequest previousRequest = deploymentRequestRepository
                .findLatestSuccessfulDeployment(dto.getApplicationName())
                .orElse(null);

        DeploymentRequest request = DeploymentRequest.builder()
                .applicationName(dto.getApplicationName())
                .componentArtifact(artifact)
                .deploymentMethod(method)
                .status(DeploymentStatus.PENDING)
                .requestedBy(requestor)
                .requestNotes(dto.getRequestNotes())
                .previousDeploymentRequest(previousRequest)
                .build();

        return toDTO(deploymentRequestRepository.save(request));
    }

    @Transactional
    public DeploymentRequestDTO actionRequest(Long requestId, ActionDeploymentRequestDTO dto) {
        DeploymentRequest request = deploymentRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment request not found: " + requestId));

        User approver = userRepository.findById(dto.getActionedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getActionedByUserId()));

        if (approver.getRole() != UserRole.DEPLOYMENT_APPROVER) {
            throw new IllegalStateException("Only DEPLOYMENT_APPROVER users can action requests.");
        }

        if (request.getStatus() != DeploymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be actioned.");
        }

        request.setActionedBy(approver);
        request.setActionNotes(dto.getActionNotes());
        request.setActionedAt(LocalDateTime.now());

        if (dto.getApproved()) {
            request.setStatus(DeploymentStatus.APPROVED);
            log.info("Deployment request {} approved by {}", requestId, approver.getUsername());

            // If method is uDeploy, trigger immediately after approval
            if (request.getDeploymentMethod().getType() == DeploymentMethodType.UDEPLOY_PROCESS) {
                triggerUDeployDeployment(request);
            }
        } else {
            request.setStatus(DeploymentStatus.REJECTED);
            log.info("Deployment request {} rejected by {}", requestId, approver.getUsername());
        }

        return toDTO(deploymentRequestRepository.save(request));
    }

    @Transactional
    public DeploymentRequestDTO triggerDeployment(Long requestId, Long userId) {
        DeploymentRequest request = deploymentRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment request not found: " + requestId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getRole() != UserRole.DEPLOYMENT_APPROVER) {
            throw new IllegalStateException("Only DEPLOYMENT_APPROVER users can trigger deployments.");
        }

        if (request.getStatus() != DeploymentStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED requests can be triggered.");
        }

        triggerUDeployDeployment(request);
        return toDTO(deploymentRequestRepository.save(request));
    }

    private void triggerUDeployDeployment(DeploymentRequest request) {
        try {
            request.setStatus(DeploymentStatus.IN_PROGRESS);
            ComponentArtifact artifact = request.getComponentArtifact();
            DeploymentMethod method = request.getDeploymentMethod();

            String udeployRequestId = uDeployService.triggerDeployment(
                    request.getApplicationName(),
                    method.getProcessIdentifier(),
                    method.getTargetEnvironment(),
                    Map.of(artifact.getComponentName(), artifact.getArtifactVersion())
            );

            request.setUdeployRequestId(udeployRequestId);
            request.setDeployedAt(LocalDateTime.now());
            request.setStatus(DeploymentStatus.SUCCESS);
            log.info("uDeploy deployment triggered, requestId: {}", udeployRequestId);
        } catch (Exception e) {
            request.setStatus(DeploymentStatus.FAILED);
            log.error("Failed to trigger uDeploy deployment for request {}: {}", request.getId(), e.getMessage());
        }
    }

    public List<DeploymentRequestDTO> getAllRequests() {
        return deploymentRequestRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DeploymentRequestDTO> getPendingRequests() {
        return deploymentRequestRepository.findAllPending().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DeploymentRequestDTO> getRequestsByApplication(String applicationName) {
        return deploymentRequestRepository.findByApplicationNameOrderByCreatedAtDesc(applicationName)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public DeploymentRequestDTO getById(Long id) {
        return deploymentRequestRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Deployment request not found: " + id));
    }

    private DeploymentRequestDTO toDTO(DeploymentRequest r) {
        DeploymentRequestDTO dto = new DeploymentRequestDTO();
        dto.setId(r.getId());
        dto.setApplicationName(r.getApplicationName());
        dto.setStatus(r.getStatus());
        dto.setRequestNotes(r.getRequestNotes());
        dto.setActionNotes(r.getActionNotes());
        dto.setUdeployRequestId(r.getUdeployRequestId());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setActionedAt(r.getActionedAt());
        dto.setDeployedAt(r.getDeployedAt());
        if (r.getPreviousDeploymentRequest() != null) {
            dto.setPreviousDeploymentRequestId(r.getPreviousDeploymentRequest().getId());
        }
        // Map nested objects via their own DTOs (shallow)
        if (r.getRequestedBy() != null) {
            var u = new com.deploytrack.dto.UserDTO();
            u.setId(r.getRequestedBy().getId());
            u.setUsername(r.getRequestedBy().getUsername());
            u.setFullName(r.getRequestedBy().getFullName());
            u.setRole(r.getRequestedBy().getRole());
            dto.setRequestedBy(u);
        }
        if (r.getActionedBy() != null) {
            var u = new com.deploytrack.dto.UserDTO();
            u.setId(r.getActionedBy().getId());
            u.setUsername(r.getActionedBy().getUsername());
            u.setFullName(r.getActionedBy().getFullName());
            u.setRole(r.getActionedBy().getRole());
            dto.setActionedBy(u);
        }
        if (r.getComponentArtifact() != null) {
            var a = new com.deploytrack.dto.ComponentArtifactDTO();
            a.setId(r.getComponentArtifact().getId());
            a.setApplicationName(r.getComponentArtifact().getApplicationName());
            a.setComponentName(r.getComponentArtifact().getComponentName());
            a.setArtifactVersion(r.getComponentArtifact().getArtifactVersion());
            a.setUdeployVersionId(r.getComponentArtifact().getUdeployVersionId());
            dto.setComponentArtifact(a);
        }
        if (r.getDeploymentMethod() != null) {
            var m = new com.deploytrack.dto.DeploymentMethodDTO();
            m.setId(r.getDeploymentMethod().getId());
            m.setName(r.getDeploymentMethod().getName());
            m.setType(r.getDeploymentMethod().getType());
            m.setTargetEnvironment(r.getDeploymentMethod().getTargetEnvironment());
            dto.setDeploymentMethod(m);
        }
        return dto;
    }
}
