package com.deploytrack.repository;

import com.deploytrack.enums.DeploymentStatus;
import com.deploytrack.model.DeploymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentRequestRepository extends JpaRepository<DeploymentRequest, Long> {

    List<DeploymentRequest> findByApplicationNameOrderByCreatedAtDesc(String applicationName);

    List<DeploymentRequest> findByStatusOrderByCreatedAtDesc(DeploymentStatus status);

    List<DeploymentRequest> findByRequestedByIdOrderByCreatedAtDesc(Long userId);

    List<DeploymentRequest> findByActionedByIdOrderByCreatedAtDesc(Long userId);

    /** Find the most recent successful deployment for an application */
    @Query("SELECT dr FROM DeploymentRequest dr WHERE dr.applicationName = :appName " +
           "AND dr.status = 'SUCCESS' ORDER BY dr.deployedAt DESC")
    Optional<DeploymentRequest> findLatestSuccessfulDeployment(@Param("appName") String applicationName);

    /** Find all pending requests ordered by creation time */
    @Query("SELECT dr FROM DeploymentRequest dr WHERE dr.status = 'PENDING' ORDER BY dr.createdAt ASC")
    List<DeploymentRequest> findAllPending();
}
