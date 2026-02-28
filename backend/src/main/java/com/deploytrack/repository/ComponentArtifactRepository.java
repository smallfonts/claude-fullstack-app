package com.deploytrack.repository;

import com.deploytrack.model.ComponentArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentArtifactRepository extends JpaRepository<ComponentArtifact, Long> {
    List<ComponentArtifact> findByApplicationName(String applicationName);
    List<ComponentArtifact> findByComponentName(String componentName);
    Optional<ComponentArtifact> findByUdeployVersionId(String udeployVersionId);
}
