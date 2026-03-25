package com.smartsure.policyservice.service;

import com.smartsure.policyservice.entity.Policy;

import java.util.List;

public interface PolicyService {
    Policy createPolicy(Policy policy, String email);

    List<Policy> getAllPolicies();

    Policy getPolicy(Long id);

    void deletePolicy(Long id);
}
