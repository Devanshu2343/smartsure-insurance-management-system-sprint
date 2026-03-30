package com.insurance.claimsservice.service.impl;

import com.insurance.claimsservice.client.AuthClient;
import com.insurance.claimsservice.client.PolicyClient;
import com.insurance.claimsservice.dto.ClaimResponse;
import com.insurance.claimsservice.dto.ClaimStatusResponse;
import com.insurance.claimsservice.dto.ClaimSummaryResponse;
import com.insurance.claimsservice.dto.InitiateClaimRequest;
import com.insurance.claimsservice.dto.PolicyInfoResponse;
import com.insurance.claimsservice.dto.UploadClaimRequest;
import com.insurance.claimsservice.dto.event.ClaimReviewedEvent;
import com.insurance.claimsservice.dto.event.ClaimSubmittedEvent;
import com.insurance.claimsservice.entity.Claim;
import com.insurance.claimsservice.entity.ClaimStatus;
import com.insurance.claimsservice.exception.BadRequestException;
import com.insurance.claimsservice.exception.ForbiddenException;
import com.insurance.claimsservice.exception.ResourceNotFoundException;
import com.insurance.claimsservice.exception.UnauthorizedException;
import com.insurance.claimsservice.exception.UserNotFoundException;
import com.insurance.claimsservice.repository.ClaimRepository;
import com.insurance.claimsservice.service.ClaimEventPublisher;
import com.insurance.claimsservice.service.ClaimService;
import feign.FeignException;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_CUSTOMER = "CUSTOMER";

    private final ClaimRepository claimRepository;
    private final PolicyClient policyClient;
    private final AuthClient authClient;
    private final MeterRegistry meterRegistry;
    private final ClaimEventPublisher claimEventPublisher;

    @Override
    public ClaimResponse initiateClaim(InitiateClaimRequest request, String userEmailHeader, String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_CUSTOMER);
        String customerEmail = resolveCustomerEmail(request.getCustomerEmail(), userEmailHeader);
        validateUserExists(customerEmail);

        PolicyInfoResponse policyInfo = fetchPolicy(request.getPolicyId());
        validatePolicyForClaim(policyInfo, customerEmail);

        Claim claim = new Claim();
        claim.setClaimNumber(generateClaimNumber());
        claim.setPolicyId(request.getPolicyId());
        claim.setCustomerEmail(customerEmail);
        claim.setClaimAmount(request.getClaimAmount());
        claim.setDescription(request.getDescription().trim());
        claim.setStatus(ClaimStatus.DRAFT);

        Claim savedClaim = claimRepository.save(claim);
        log.info("Claim initiated successfully. claimNumber={}, policyId={}",
                savedClaim.getClaimNumber(),
                savedClaim.getPolicyId());

        return mapToResponse(savedClaim);
    }

    @Override
    public ClaimResponse uploadClaimDocument(UploadClaimRequest request, String userEmailHeader, String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_CUSTOMER);
        String customerEmail = requireEmail(userEmailHeader);
        Claim claim = claimRepository.findById(request.getClaimId())
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + request.getClaimId()));

        if (!claim.getCustomerEmail().equalsIgnoreCase(customerEmail)) {
            throw new ForbiddenException("Access denied for claim: " + claim.getClaimNumber());
        }

        if (claim.getStatus() == ClaimStatus.APPROVED
                || claim.getStatus() == ClaimStatus.REJECTED
                || claim.getStatus() == ClaimStatus.CLOSED) {
            throw new BadRequestException("Document upload is not allowed for final claim state: " + claim.getStatus());
        }

        boolean shouldPublishSubmitted = claim.getStatus() == ClaimStatus.DRAFT;
        claim.setDocumentUrl(request.getDocumentUrl().trim());
        if (shouldPublishSubmitted) {
            claim.setStatus(ClaimStatus.SUBMITTED);
            meterRegistry.counter("claims.submitted", "service", "claims-service").increment();
        }

        Claim updatedClaim = claimRepository.save(claim);
        if (shouldPublishSubmitted) {
            claimEventPublisher.publishClaimSubmitted(new ClaimSubmittedEvent(
                    updatedClaim.getId(),
                    updatedClaim.getClaimNumber(),
                    updatedClaim.getPolicyId(),
                    updatedClaim.getCustomerEmail(),
                    updatedClaim.getClaimAmount(),
                    updatedClaim.getStatus().name(),
                    updatedClaim.getUpdatedAt()
            ));
        }
        log.info("Claim document uploaded. claimNumber={}, status={}", updatedClaim.getClaimNumber(), updatedClaim.getStatus());

        return mapToResponse(updatedClaim);
    }

    @Override
    public ClaimStatusResponse getClaimStatus(Long claimId, String userEmailHeader, String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_CUSTOMER);
        String customerEmail = requireEmail(userEmailHeader);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        if (!claim.getCustomerEmail().equalsIgnoreCase(customerEmail)) {
            throw new ForbiddenException("Access denied for claim: " + claim.getClaimNumber());
        }

        return ClaimStatusResponse.builder()
                .claimId(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .status(claim.getStatus())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    @Override
    public ClaimResponse reviewClaim(Long claimId, String decision, String remarks, String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_ADMIN);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        if (claim.getStatus() == ClaimStatus.CLOSED) {
            throw new BadRequestException("Claim is already closed and cannot be reviewed");
        }

        if (claim.getStatus() == ClaimStatus.DRAFT) {
            throw new BadRequestException("Claim must be submitted before review");
        }

        String normalizedDecision = decision == null ? "" : decision.trim().toUpperCase(Locale.ROOT);
        if (!"APPROVED".equals(normalizedDecision) && !"REJECTED".equals(normalizedDecision)) {
            throw new BadRequestException("Decision must be APPROVED or REJECTED");
        }

        if (claim.getStatus() == ClaimStatus.APPROVED || claim.getStatus() == ClaimStatus.REJECTED) {
            throw new BadRequestException("Claim is already reviewed");
        }

        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        if ("APPROVED".equals(normalizedDecision)) {
            claim.setStatus(ClaimStatus.APPROVED);
        } else {
            claim.setStatus(ClaimStatus.REJECTED);
        }

        Claim updatedClaim = claimRepository.save(claim);
        claimEventPublisher.publishClaimReviewed(new ClaimReviewedEvent(
                updatedClaim.getId(),
                updatedClaim.getClaimNumber(),
                updatedClaim.getStatus().name(),
                normalizedDecision,
                updatedClaim.getUpdatedAt()
        ));
        log.info("Claim reviewed. claimNumber={}, decision={}, remarks={}",
                updatedClaim.getClaimNumber(),
                normalizedDecision,
                remarks);

        return mapToResponse(updatedClaim);
    }

    @Override
    public ClaimSummaryResponse getClaimSummary(String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_ADMIN);
        long totalClaims = claimRepository.count();
        long approvedClaims = claimRepository.countByStatus(ClaimStatus.APPROVED);
        long rejectedClaims = claimRepository.countByStatus(ClaimStatus.REJECTED);
        long underReviewClaims = claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW);

        return ClaimSummaryResponse.builder()
                .totalClaims(totalClaims)
                .approvedClaims(approvedClaims)
                .rejectedClaims(rejectedClaims)
                .underReviewClaims(underReviewClaims)
                .build();
    }

    private PolicyInfoResponse fetchPolicy(Long policyId) {
        try {
            return policyClient.getPolicyById(policyId);
        } catch (FeignException.NotFound exception) {
            throw new BadRequestException("Cannot initiate claim. Policy not found: " + policyId);
        } catch (FeignException.Forbidden exception) {
            throw new ForbiddenException("Access denied for policy: " + policyId);
        } catch (FeignException exception) {
            throw new BadRequestException("Cannot validate policy at this moment. Please try again");
        }
    }

    private void validatePolicyForClaim(PolicyInfoResponse policyInfo, String customerEmail) {
        if (policyInfo == null || policyInfo.status() == null) {
            throw new BadRequestException("Cannot initiate claim. Invalid policy details");
        }

        String policyStatus = policyInfo.status().trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(policyStatus)) {
            throw new BadRequestException("Claims are allowed only for ACTIVE policies");
        }

        if (policyInfo.customerEmail() == null
                || !policyInfo.customerEmail().equalsIgnoreCase(customerEmail)) {
            throw new ForbiddenException("Policy does not belong to the authenticated customer");
        }
    }

    private String resolveCustomerEmail(String requestEmail, String userEmailHeader) {
        String headerEmail = requireEmail(userEmailHeader);
        if (requestEmail != null && !requestEmail.isBlank()) {
            String normalized = requestEmail.trim().toLowerCase(Locale.ROOT);
            if (!normalized.equalsIgnoreCase(headerEmail)) {
                throw new ForbiddenException("Customer email mismatch with authenticated user");
            }
        }
        return headerEmail;
    }

    private String requireEmail(String userEmailHeader) {
        if (userEmailHeader == null || userEmailHeader.isBlank()) {
            throw new UnauthorizedException("Missing user email header");
        }
        return userEmailHeader.trim().toLowerCase(Locale.ROOT);
    }

    private void requireRole(String userRoleHeader, String requiredRole) {
        if (userRoleHeader == null || userRoleHeader.isBlank()) {
            throw new UnauthorizedException("Missing user role header");
        }
        if (!requiredRole.equalsIgnoreCase(userRoleHeader)) {
            throw new ForbiddenException("Access denied for role: " + userRoleHeader);
        }
    }

    private void validateUserExists(String email) {
        try {
            Boolean exists = authClient.validateUser(email);
            if (!Boolean.TRUE.equals(exists)) {
                throw new UserNotFoundException("User not registered. Cannot initiate claim");
            }
        } catch (FeignException.NotFound exception) {
            throw new UserNotFoundException("User not registered. Cannot initiate claim");
        } catch (FeignException exception) {
            throw new BadRequestException("Unable to validate user at the moment");
        }
    }

    private String generateClaimNumber() {
        return "CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private ClaimResponse mapToResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicyId())
                .customerEmail(claim.getCustomerEmail())
                .claimAmount(claim.getClaimAmount())
                .description(claim.getDescription())
                .documentUrl(claim.getDocumentUrl())
                .status(claim.getStatus())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
