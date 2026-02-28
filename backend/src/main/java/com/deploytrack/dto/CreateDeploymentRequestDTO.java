package com.deploytrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDeploymentRequestDTO {

    @NotBlank
    private String applicationName;

    @NotNull
    private Long componentArtifactId;

    @NotNull
    private Long deploymentMethodId;

    @NotNull
    private Long requestedByUserId;

    private String requestNotes;
}
