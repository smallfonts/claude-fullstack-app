package com.deploytrack.repository;

import com.deploytrack.model.DeploymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeploymentMethodRepository extends JpaRepository<DeploymentMethod, Long> {
    List<DeploymentMethod> findByActiveTrue();
}
