package com.insurance.claimsservice.repository;

import com.insurance.claimsservice.entity.Claim;
import com.insurance.claimsservice.entity.ClaimStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    long countByStatus(ClaimStatus status);
}
