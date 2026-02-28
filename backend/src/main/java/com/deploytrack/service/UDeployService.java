package com.deploytrack.service;

import com.deploytrack.config.UDeployProperties;
import com.deploytrack.dto.ComponentArtifactDTO;
import com.deploytrack.model.ComponentArtifact;
import com.deploytrack.repository.ComponentArtifactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UDeployService {

    private final WebClient udeployWebClient;
    private final ComponentArtifactRepository componentArtifactRepository;
    private final UDeployProperties uDeployProperties;

    /**
     * Fetches the list of components for a given application from uDeploy.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getApplicationComponents(String applicationName, String authToken) {
        log.info("Fetching components for application: {}", applicationName);
        try {
            List<Map<String, Object>> components = udeployWebClient.get()
                    .uri("/rest/deploy/application/{name}/components", applicationName)
                    .header("Authorization", "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            return components != null ? components : new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to fetch components for application {}: {}", applicationName, e.getMessage());
            throw new RuntimeException("Failed to fetch components from uDeploy: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches all versions (artifacts) for a given component from uDeploy.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getComponentVersions(String componentId, String authToken) {
        log.info("Fetching versions for component: {}", componentId);
        try {
            List<Map<String, Object>> versions = udeployWebClient.get()
                    .uri("/rest/deploy/component/{id}/versions/false", componentId)
                    .header("Authorization", "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            return versions != null ? versions : new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to fetch versions for component {}: {}", componentId, e.getMessage());
            throw new RuntimeException("Failed to fetch component versions from uDeploy: " + e.getMessage(), e);
        }
    }

    /**
     * Syncs component artifacts from uDeploy into the local database for a given application.
     */
    @SuppressWarnings("unchecked")
    public List<ComponentArtifact> syncArtifactsForApplication(String applicationName, String authToken) {
        log.info("Syncing artifacts for application: {}", applicationName);
        List<Map<String, Object>> components = getApplicationComponents(applicationName, authToken);
        List<ComponentArtifact> synced = new ArrayList<>();

        for (Map<String, Object> component : components) {
            String componentId = (String) component.get("id");
            String componentName = (String) component.get("name");

            List<Map<String, Object>> versions = getComponentVersions(componentId, authToken);
            for (Map<String, Object> version : versions) {
                String versionId = (String) version.get("id");
                String versionName = (String) version.get("name");

                ComponentArtifact artifact = componentArtifactRepository
                        .findByUdeployVersionId(versionId)
                        .orElse(new ComponentArtifact());

                artifact.setApplicationName(applicationName);
                artifact.setComponentName(componentName);
                artifact.setArtifactVersion(versionName);
                artifact.setUdeployComponentId(componentId);
                artifact.setUdeployVersionId(versionId);
                artifact.setSyncedAt(LocalDateTime.now());

                synced.add(componentArtifactRepository.save(artifact));
            }
        }

        log.info("Synced {} artifacts for application {}", synced.size(), applicationName);
        return synced;
    }

    /**
     * Triggers a uDeploy application process deployment.
     *
     * @param applicationName   uDeploy application name
     * @param applicationProcess uDeploy application process name
     * @param environmentName   target environment name
     * @param componentVersions map of componentName -> versionName to deploy
     * @return the uDeploy deployment request ID
     */
    @SuppressWarnings("unchecked")
    public String triggerDeployment(String applicationName,
                                    String applicationProcess,
                                    String environmentName,
                                    Map<String, String> componentVersions,
                                    String authToken) {
        log.info("Triggering uDeploy deployment: app={}, process={}, env={}",
                applicationName, applicationProcess, environmentName);

        // Build uDeploy deployment request payload
        Map<String, Object> payload = Map.of(
                "application", applicationName,
                "applicationProcess", applicationProcess,
                "environment", environmentName,
                "versions", componentVersions
        );

        try {
            Map<String, Object> response = udeployWebClient.put()
                    .uri("/rest/deploy/application/request")
                    .header("Authorization", "Bearer " + authToken)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("requestId")) {
                String requestId = (String) response.get("requestId");
                log.info("uDeploy deployment triggered, requestId: {}", requestId);
                return requestId;
            }
            throw new RuntimeException("uDeploy response did not contain a requestId");
        } catch (Exception e) {
            log.error("Failed to trigger uDeploy deployment: {}", e.getMessage());
            throw new RuntimeException("Failed to trigger deployment in uDeploy: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the status of an existing uDeploy deployment request.
     */
    @SuppressWarnings("unchecked")
    public String getDeploymentStatus(String udeployRequestId, String authToken) {
        try {
            Map<String, Object> response = udeployWebClient.get()
                    .uri("/rest/deploy/application/request/{id}", udeployRequestId)
                    .header("Authorization", "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("status")) {
                return (String) response.get("status");
            }
            return "UNKNOWN";
        } catch (Exception e) {
            log.error("Failed to get deployment status for requestId {}: {}", udeployRequestId, e.getMessage());
            return "ERROR";
        }
    }
}
