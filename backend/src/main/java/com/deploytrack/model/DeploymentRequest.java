package com.deploytrack.model;

import com.deploytrack.enums.DeploymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deployment_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeploymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The uDeploy application name being deployed */
    @Column(nullable = false)
    private String applicationName;

    /** The artifact to be deployed */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_artifact_id", nullable = false)
    private ComponentArtifact componentArtifact;

    /** How this deployment will be executed */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_method_id", nullable = false)
    private DeploymentMethod deploymentMethod;

    /** Current status of this deployment request */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeploymentStatus status;

    /** User who raised this deployment request */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    /** User who approved or rejected this request */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actioned_by_user_id")
    private User actionedBy;

    /** Notes provided by the requestor */
    @Column(length = 1000)
    private String requestNotes;

    /** Notes from the approver when actioning */
    @Column(length = 1000)
    private String actionNotes;

    /**
     * Reference to the previous deployment request for the same application.
     * Allows tracking deployment history chain.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_deployment_request_id")
    private DeploymentRequest previousDeploymentRequest;

    /** uDeploy deployment process request ID, set after triggering */
    private String udeployRequestId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime actionedAt;

    private LocalDateTime deployedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = DeploymentStatus.PENDING;
        }
    }
}
