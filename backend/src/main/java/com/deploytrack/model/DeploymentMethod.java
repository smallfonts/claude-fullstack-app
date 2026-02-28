package com.deploytrack.model;

import com.deploytrack.enums.DeploymentMethodType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "deployment_method")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeploymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeploymentMethodType type;

    /**
     * For UDEPLOY_PROCESS type: the name of the uDeploy application process to invoke.
     * For SCRIPTED: the script identifier.
     */
    private String processIdentifier;

    /** Target environment in uDeploy (e.g., DEV, UAT, PROD) */
    private String targetEnvironment;

    private boolean active = true;
}
