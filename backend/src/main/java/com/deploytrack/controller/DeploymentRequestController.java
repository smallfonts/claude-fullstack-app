package com.deploytrack.controller;

import com.deploytrack.dto.ActionDeploymentRequestDTO;
import com.deploytrack.dto.CreateDeploymentRequestDTO;
import com.deploytrack.dto.DeploymentRequestDTO;
import com.deploytrack.service.DeploymentRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deployment-requests")
@RequiredArgsConstructor
public class DeploymentRequestController {

    private final DeploymentRequestService service;

    @GetMapping
    public ResponseEntity<List<DeploymentRequestDTO>> getAll(
            @RequestParam(required = false) String applicationName) {
        if (applicationName != null) {
            return ResponseEntity.ok(service.getRequestsByApplication(applicationName));
        }
        return ResponseEntity.ok(service.getAllRequests());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<DeploymentRequestDTO>> getPending() {
        return ResponseEntity.ok(service.getPendingRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeploymentRequestDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<DeploymentRequestDTO> create(@Valid @RequestBody CreateDeploymentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRequest(dto));
    }

    @PostMapping("/{id}/action")
    public ResponseEntity<DeploymentRequestDTO> action(
            @PathVariable Long id,
            @Valid @RequestBody ActionDeploymentRequestDTO dto) {
        return ResponseEntity.ok(service.actionRequest(id, dto));
    }

    @PostMapping("/{id}/trigger")
    public ResponseEntity<DeploymentRequestDTO> trigger(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(service.triggerDeployment(id, userId));
    }
}
