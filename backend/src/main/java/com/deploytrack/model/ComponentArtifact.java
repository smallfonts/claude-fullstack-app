package com.deploytrack.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "component_artifact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComponentArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The uDeploy application name this artifact belongs to */
    @Column(nullable = false)
    private String applicationName;

    /** The uDeploy component name */
    @Column(nullable = false)
    private String componentName;

    /** The artifact version/name as returned by uDeploy */
    @Column(nullable = false)
    private String artifactVersion;

    /** The uDeploy component ID (UUID from uDeploy) */
    private String udeployComponentId;

    /** The uDeploy version ID */
    private String udeployVersionId;

    private String description;

    /** When this artifact record was synced from uDeploy */
    @Column(nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist
    protected void onCreate() {
        syncedAt = LocalDateTime.now();
    }
}
