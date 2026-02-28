package com.deploytrack.dto;

import com.deploytrack.enums.DeploymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeploymentRequestDTO {
    private Long id;
    private String applicationName;
    private ComponentArtifactDTO componentArtifact;
    private DeploymentMethodDTO deploymentMethod;
    private DeploymentStatus status;
    private UserDTO requestedBy;
    private UserDTO actionedBy;
    private String requestNotes;
    private String actionNotes;
    private Long previousDeploymentRequestId;
    private String udeployRequestId;
    private LocalDateTime createdAt;
    private LocalDateTime actionedAt;
    private LocalDateTime deployedAt;
}
