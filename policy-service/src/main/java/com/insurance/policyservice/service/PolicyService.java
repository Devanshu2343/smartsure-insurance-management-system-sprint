package com.insurance.policyservice.service;

import com.insurance.policyservice.dto.PolicyResponse;
import com.insurance.policyservice.dto.PolicySummaryResponse;
import com.insurance.policyservice.dto.PurchasePolicyRequest;
import java.util.List;

public interface PolicyService {

    /**
     * Purchases a policy for the authenticated customer.
     *
     * @param request policy purchase request
     * @param userEmailHeader authenticated email from gateway header
     * @param userRoleHeader authenticated role from gateway header
     * @return created policy response
     */
    PolicyResponse purchasePolicy(PurchasePolicyRequest request, String userEmailHeader, String userRoleHeader);

    /**
     * Fetches a policy by id, enforcing customer ownership or admin access.
     *
     * @param policyId policy id
     * @param userEmailHeader authenticated email from gateway header
     * @param userRoleHeader authenticated role from gateway header
     * @return policy response
     */
    PolicyResponse getPolicyById(Long policyId, String userEmailHeader, String userRoleHeader);

    /**
     * Returns policies belonging to the authenticated customer.
     *
     * @param userEmailHeader authenticated email from gateway header
     * @param userRoleHeader authenticated role from gateway header
     * @return list of policies
     */
    List<PolicyResponse> getPoliciesForCustomer(String userEmailHeader, String userRoleHeader);

    /**
     * Creates a policy as an admin.
     *
     * @param request policy request
     * @param userRoleHeader authenticated role from gateway header
     * @return created policy response
     */
    PolicyResponse createPolicyForAdmin(PurchasePolicyRequest request, String userRoleHeader);

    /**
     * Updates a policy as an admin.
     *
     * @param policyId policy id
     * @param request policy request
     * @param userRoleHeader authenticated role from gateway header
     * @return updated policy response
     */
    PolicyResponse updatePolicyForAdmin(Long policyId, PurchasePolicyRequest request, String userRoleHeader);

    /**
     * Deletes a policy as an admin.
     *
     * @param policyId policy id
     * @param userRoleHeader authenticated role from gateway header
     */
    void deletePolicyForAdmin(Long policyId, String userRoleHeader);

    /**
     * Returns summary metrics for policies.
     *
     * @param userRoleHeader authenticated role from gateway header
     * @return summary response
     */
    PolicySummaryResponse getPolicySummary(String userRoleHeader);
}
