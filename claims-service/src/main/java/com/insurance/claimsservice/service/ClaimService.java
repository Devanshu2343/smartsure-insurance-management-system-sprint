package com.insurance.claimsservice.service;

import com.insurance.claimsservice.dto.ClaimResponse;
import com.insurance.claimsservice.dto.ClaimStatusResponse;
import com.insurance.claimsservice.dto.ClaimSummaryResponse;
import com.insurance.claimsservice.dto.InitiateClaimRequest;
import com.insurance.claimsservice.dto.UploadClaimRequest;

public interface ClaimService {

    /**
     * Initiates a claim for a customer after validating user and policy.
     *
     * @param request claim initiation request
     * @param userEmailHeader authenticated email
     * @param userRoleHeader authenticated role
     * @return created claim response
     */
    ClaimResponse initiateClaim(InitiateClaimRequest request, String userEmailHeader, String userRoleHeader);

    /**
     * Uploads claim document for a customer-owned claim.
     *
     * @param request upload request
     * @param userEmailHeader authenticated email
     * @param userRoleHeader authenticated role
     * @return updated claim response
     */
    ClaimResponse uploadClaimDocument(UploadClaimRequest request, String userEmailHeader, String userRoleHeader);

    /**
     * Retrieves claim status for a customer-owned claim.
     *
     * @param claimId claim id
     * @param userEmailHeader authenticated email
     * @param userRoleHeader authenticated role
     * @return claim status response
     */
    ClaimStatusResponse getClaimStatus(Long claimId, String userEmailHeader, String userRoleHeader);

    /**
     * Reviews a claim as an admin.
     *
     * @param claimId claim id
     * @param decision decision value
     * @param remarks optional remarks
     * @param userRoleHeader authenticated role
     * @return updated claim response
     */
    ClaimResponse reviewClaim(Long claimId, String decision, String remarks, String userRoleHeader);

    /**
     * Returns summary metrics for claims.
     *
     * @param userRoleHeader authenticated role
     * @return claim summary response
     */
    ClaimSummaryResponse getClaimSummary(String userRoleHeader);
}
