package com.insurance.policyservice.repository;

import com.insurance.policyservice.entity.Policy;
import com.insurance.policyservice.entity.PolicyStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findAllByCustomerEmailIgnoreCase(String customerEmail);

    long countByStatus(PolicyStatus status);
}
