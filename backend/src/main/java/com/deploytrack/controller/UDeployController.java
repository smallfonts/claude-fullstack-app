package com.deploytrack.controller;

import com.deploytrack.model.ComponentArtifact;
import com.deploytrack.model.User;
import com.deploytrack.repository.UserRepository;
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
    private final UserRepository userRepository;

    private String resolveToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (user.getUdeployAuthToken() == null || user.getUdeployAuthToken().isBlank()) {
            throw new IllegalStateException("User " + user.getUsername() + " has no uDeploy auth token configured.");
        }
        return user.getUdeployAuthToken();
    }

    /** Get raw components for an application directly from uDeploy */
    @GetMapping("/applications/{applicationName}/components")
    public ResponseEntity<List<Map<String, Object>>> getComponents(
            @PathVariable String applicationName,
            @RequestParam Long userId) {
        return ResponseEntity.ok(uDeployService.getApplicationComponents(applicationName, resolveToken(userId)));
    }

    /** Get raw versions for a component directly from uDeploy */
    @GetMapping("/components/{componentId}/versions")
    public ResponseEntity<List<Map<String, Object>>> getVersions(
            @PathVariable String componentId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(uDeployService.getComponentVersions(componentId, resolveToken(userId)));
    }

    /** Sync artifacts from uDeploy into local DB for a given application */
    @PostMapping("/applications/{applicationName}/sync")
    public ResponseEntity<List<ComponentArtifact>> syncArtifacts(
            @PathVariable String applicationName,
            @RequestParam Long userId) {
        return ResponseEntity.ok(uDeployService.syncArtifactsForApplication(applicationName, resolveToken(userId)));
    }

    /** Check status of a uDeploy deployment request */
    @GetMapping("/requests/{udeployRequestId}/status")
    public ResponseEntity<Map<String, String>> getDeploymentStatus(
            @PathVariable String udeployRequestId,
            @RequestParam Long userId) {
        String status = uDeployService.getDeploymentStatus(udeployRequestId, resolveToken(userId));
        return ResponseEntity.ok(Map.of("status", status, "requestId", udeployRequestId));
    }
}
