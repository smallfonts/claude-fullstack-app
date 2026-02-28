package com.deploytrack.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActionDeploymentRequestDTO {

    @NotNull
    private Long actionedByUserId;

    /** true = approve, false = reject */
    @NotNull
    private Boolean approved;

    private String actionNotes;
}
