package com.deploytrack.dto;

import com.deploytrack.enums.DeploymentMethodType;
import lombok.Data;

@Data
public class DeploymentMethodDTO {
    private Long id;
    private String name;
    private String description;
    private DeploymentMethodType type;
    private String processIdentifier;
    private String targetEnvironment;
    private boolean active;
}
