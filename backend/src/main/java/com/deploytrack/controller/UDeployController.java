package com.deploytrack.controller;

import com.deploytrack.model.ComponentArtifact;
import com.deploytrack.service.UDeployService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/udeploy")
@RequiredArgsConstructor
public class UDeployController {

    private final UDeployService uDeployService;

    /** Get raw components for an application directly from uDeploy */
    @GetMapping("/applications/{applicationName}/components")
    public ResponseEntity<List<Map<String, Object>>> getComponents(
            @PathVariable String applicationName) {
        return ResponseEntity.ok(uDeployService.getApplicationComponents(applicationName));
    }

    /** Get raw versions for a component directly from uDeploy */
    @GetMapping("/components/{componentId}/versions")
    public ResponseEntity<List<Map<String, Object>>> getVersions(
            @PathVariable String componentId) {
        return ResponseEntity.ok(uDeployService.getComponentVersions(componentId));
    }

    /** Sync artifacts from uDeploy into local DB for a given application */
    @PostMapping("/applications/{applicationName}/sync")
    public ResponseEntity<List<ComponentArtifact>> syncArtifacts(
            @PathVariable String applicationName) {
        return ResponseEntity.ok(uDeployService.syncArtifactsForApplication(applicationName));
    }

    /** Check status of a uDeploy deployment request */
    @GetMapping("/requests/{udeployRequestId}/status")
    public ResponseEntity<Map<String, String>> getDeploymentStatus(
            @PathVariable String udeployRequestId) {
        String status = uDeployService.getDeploymentStatus(udeployRequestId);
        return ResponseEntity.ok(Map.of("status", status, "requestId", udeployRequestId));
    }
}
