package com.deploytrack.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComponentArtifactDTO {
    private Long id;
    private String applicationName;
    private String componentName;
    private String artifactVersion;
    private String udeployComponentId;
    private String udeployVersionId;
    private String description;
    private LocalDateTime syncedAt;
}
